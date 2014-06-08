package mynuaa.whatever;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class StartupActivity extends SherlockFragmentActivity {

	private static final int MESSAGE_SPLASHFINISHED = 1;

	SplashFragment mSplashFragment = new SplashFragment();
	LoginFragment mLoginFragment = new LoginFragment();

	MyHandler mHandler = new MyHandler(this);

	private static class MyHandler extends Handler {
		WeakReference<StartupActivity> mStartupActivity;

		public MyHandler(StartupActivity activity) {
			mStartupActivity = new WeakReference<StartupActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			StartupActivity mainActivity = mStartupActivity.get();

			if (mainActivity != null) {
				switch (msg.what) {
				case MESSAGE_SPLASHFINISHED: {
					mainActivity.onSplashFinished();
					break;
				}
				}
			}
			super.handleMessage(msg);
		}
	}

	boolean isSplashFinished = false;
	private boolean isPaused = true;
	private boolean isOnSplashFinishedPosted = false;
	private Object fragmentSyncObj = new Object();

	protected void onSplashFinished() {
		synchronized (fragmentSyncObj) {
			if (!isSplashFinished) {
				if (isPaused) {
					isOnSplashFinishedPosted = true;
				} else {
					isOnSplashFinishedPosted = false;
					FragmentTransaction ft = getSupportFragmentManager()
							.beginTransaction();
					ft.setCustomAnimations(R.anim.slide_in_bottom,
							R.anim.slide_out_top);
					ft.replace(R.id.content_frame, mLoginFragment);
					ft.commit();
					isSplashFinished = true;
				}
			}
		}
	}

	protected void postOnSplashFinished() {
		synchronized (fragmentSyncObj) {
			if (!isSplashFinished) {
				mHandler.sendEmptyMessage(MESSAGE_SPLASHFINISHED);
			}
		}
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		synchronized (fragmentSyncObj) {
			isPaused = false;
			if (isOnSplashFinishedPosted) {
				onSplashFinished();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		synchronized (fragmentSyncObj) {
			isPaused = true;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// hack dns
		// NetworkHelper.addDNSCache("my.nuaa.edu.cn", "222.192.100.21");

		Util.createShortCut(this);

		setContentView(R.layout.activity_startup);

		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top);
			ft.replace(R.id.content_frame, mSplashFragment);
			ft.commit();
		}

		if (getIntent().getBooleanExtra("skipSplash", false)) {
			postOnSplashFinished();
		}

	}

	private OnKeyListener mOnKeyListener = null;

	public void setOnKeyUpListener(OnKeyListener l) {
		mOnKeyListener = l;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (mOnKeyListener != null) {
			if (mOnKeyListener.onKey(getCurrentFocus(), keyCode, event))
				return true;
		}

		return super.onKeyUp(keyCode, event);
	}

}
