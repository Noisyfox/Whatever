package mynuaa.whatever;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class SplashFragment extends SherlockFragment implements
		OnClickListener, OnKeyListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_splash, container,
				false);

		rootView.findViewById(R.id.splash_root).setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
				Activity activity = getActivity();

				if (StartupActivity.class.isInstance(activity)) {
					((StartupActivity) activity).postOnSplashFinished();
				}
			}

		}.start();

		if (StartupActivity.class.isInstance(activity)) {
			((StartupActivity) activity).setOnKeyUpListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		Activity activity = getActivity();

		if (StartupActivity.class.isInstance(activity)) {
			((StartupActivity) activity).onSplashFinished();
		}
	}

	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
		Activity activity = getActivity();
		((StartupActivity) activity).onSplashFinished();
		return true;
	}

}
