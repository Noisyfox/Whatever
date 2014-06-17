package mynuaa.whatever;

import mynuaa.whatever.DataSource.FeedbackTask;
import mynuaa.whatever.DataSource.TaskManager;
import com.actionbarsherlock.app.SherlockActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackActivity extends SherlockActivity implements
		OnGestureListener, OnClickListener {

	GestureDetector mGestureDetector;

	private EditText mEditText_message, mEditText_contact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);

		Util.setupCommonActionBar(this, R.string.feedback_title);

		mGestureDetector = new GestureDetector(this, this);

		mEditText_message = (EditText) findViewById(R.id.editText_message);
		mEditText_contact = (EditText) findViewById(R.id.editText_contact);
		findViewById(R.id.button_send).setOnClickListener(this);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		String cachedMessage = sp.getString("feedBackM", "");
		if (!Util.isBlank(cachedMessage)) {
			mEditText_message.setText(cachedMessage);
			mEditText_contact.setText(sp.getString("feedBackC", ""));
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

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
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onClick(View v) {
		String message = mEditText_message.getText().toString();
		String contact = mEditText_contact.getText().toString();

		if (Util.isBlank(message)) {
			Toast.makeText(this, R.string.feedback_toast_no_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor e = sp.edit();
		e.putString("feedBackM", message);
		e.putString("feedBackC", contact);
		e.commit();

		WhateverApplication.getMainTaskManager().startTask(
				new FeedbackTask(TaskManager.TAG_GLOBAL, message, contact,
						WhateverApplication.getApplication()));

		Toast.makeText(this, R.string.feedback_toast_sending,
				Toast.LENGTH_SHORT).show();

		finish();
	}

}
