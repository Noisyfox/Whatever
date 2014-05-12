package mynuaa.whatever;

import mynuaa.whatever.SettingsWidget.ButtonSetting;
import mynuaa.whatever.SettingsWidget.Setting;
import mynuaa.whatever.SettingsWidget.SettingsAdapter;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AboutActivity extends SherlockActivity implements
		OnItemClickListener, OnGestureListener {

	GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		Util.setupCommonActionBar(this, R.string.about_title);

		SettingsAdapter sa = new SettingsAdapter(this, "About");
		setupSettings(sa);
		sa.loadSettings();

		ListView settingsView = (ListView) findViewById(R.id.listView_settings);
		settingsView.setAdapter(sa);
		settingsView.setOnItemClickListener(this);

		mGestureDetector = new GestureDetector(this, this);
	}

	private void setupSettings(SettingsAdapter settingsAdapter) {
		settingsAdapter.addSetting(new ButtonSetting("隐私声明", "privacy") {
			@Override
			public void OnClick() {
				WebViewActivity.startWebView(AboutActivity.this,
						"file:///android_asset/privacy.html");
			}
		});
		settingsAdapter.addSetting(new ButtonSetting("团队简介", "aboutUs") {
			@Override
			public void OnClick() {
			}
		});
		settingsAdapter.addSetting(new ButtonSetting("扫一扫 下载我", "download") {
			@Override
			public void OnClick() {
				Intent i = new Intent();
				i.setClass(AboutActivity.this, QRShareActivity.class);
				AboutActivity.this.startActivity(i);
				AboutActivity.this.overridePendingTransition(
						R.anim.slide_in_right, R.anim.stay);
			}
		});
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Setting s = (Setting) arg0.getAdapter().getItem(arg2);
		s.OnClick();
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	private int verticalMinDistance = 50;
	private int minVelocity = 0;

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float dx = e1.getX() - e2.getX(), dy = e1.getY() - e2.getY();

		if (dx > verticalMinDistance && Math.abs(velocityX) > minVelocity) {
		} else if (-dx > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity
				&& Math.abs(dx) > Math.abs(dy)) {
			finish();
		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
}
