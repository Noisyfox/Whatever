package mynuaa.whatever;

import mynuaa.whatever.DataSource.ImageLoadTask.OnImageLoadListener;
import mynuaa.whatever.DataSource.ImageLoadTask;
import mynuaa.whatever.DataSource.MessageData;
import mynuaa.whatever.DataSource.MessageMannerTask;
import mynuaa.whatever.DataSource.MessageMannerTask.OnMannerPutListener;
import mynuaa.whatever.DataSource.MessageRefreshTask;
import mynuaa.whatever.DataSource.MessageRefreshTask.OnMessageRefreshListener;
import mynuaa.whatever.DataSource.ReportTask;
import mynuaa.whatever.DataSource.WHOTask;
import mynuaa.whatever.DataSource.WHOTask.OnWHOListener;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class MessageActivity extends SherlockActivity implements
		OnGestureListener, OnImageLoadListener, OnClickListener,
		OnMessageRefreshListener, OnMannerPutListener, OnWHOListener {
	private static final String TASK_TAG = "task_message_activity";

	public static void showMessage(Activity activity, MessageData md) {
		Intent intent = new Intent();
		intent.setClass(activity, MessageActivity.class);

		intent.putExtra("message", md);

		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
	}

	public static void showMessage(Activity activity, String cid) {
		Intent intent = new Intent();
		intent.setClass(activity, MessageActivity.class);

		intent.putExtra("cid", cid);

		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
	}

	GestureDetector mGestureDetector;

	NumberButton btn_good, btn_bad, btn_comment;
	Button btn_report, btn_pm, btn_who;

	TextView textView_message, textView_time, textView_bad, textView_good;
	ImageView imageView_image, imageView_background, imageView_bad,
			imageView_good;

	View button_bottom, button_bottom_1, button_bottom_2, button_bottom_3,
			bkg_normal, bkg_trans;

	MessageData mMessage = null;
	String mCid = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);

		Util.setupCommonActionBar(this, R.string.message_title);

		mGestureDetector = new GestureDetector(this, this);

		button_bottom = findViewById(R.id.button_bottom);
		button_bottom_1 = findViewById(R.id.button_bottom_1);
		button_bottom_2 = findViewById(R.id.button_bottom_2);
		button_bottom_3 = findViewById(R.id.button_bottom_3);
		bkg_normal = findViewById(R.id.bkg_normal);
		bkg_trans = findViewById(R.id.bkg_trans);

		btn_good = (NumberButton) findViewById(R.id.button_good);
		btn_bad = (NumberButton) findViewById(R.id.button_bad);
		btn_comment = (NumberButton) findViewById(R.id.button_comment);
		btn_report = (Button) findViewById(R.id.button_report);
		btn_pm = (Button) findViewById(R.id.button_pm);
		btn_who = (Button) findViewById(R.id.button_who);

		textView_bad = (TextView) findViewById(R.id.textView_bad);
		textView_good = (TextView) findViewById(R.id.textView_good);
		imageView_bad = (ImageView) findViewById(R.id.imageView_bad);
		imageView_good = (ImageView) findViewById(R.id.imageView_good);

		textView_message = (TextView) findViewById(R.id.textView_message);
		textView_time = (TextView) findViewById(R.id.textView_time);
		imageView_image = (ImageView) findViewById(R.id.imageView_image);
		imageView_background = (ImageView) findViewById(R.id.imageView_background);

		mMessage = getIntent().getParcelableExtra("message");
		mCid = getIntent().getStringExtra("cid");

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		if (mMessage != null) {
			displayMessage();
			WhateverApplication.getMainTaskManager().startTask(
					new MessageRefreshTask(TASK_TAG, mMessage.cid, this));
		} else if (!TextUtils.isEmpty(mCid)) {
			WhateverApplication.getMainTaskManager().startTask(
					new MessageRefreshTask(TASK_TAG, mCid, this));
		}
	}

	private void displayMessage() {
		if (mMessage != null) {
			setupButtons();

			textView_message.setText(mMessage.content);
			textView_time.setText(mMessage.time);
			imageView_background.setImageBitmap(MessageTheme.createBackground(
					this, mMessage.background_color_index,
					mMessage.background_texture_index));

			textView_message.setTextColor(MessageTheme.getColor(
					mMessage.background_color_index, MessageTheme.COLOR_TEXT));
			textView_time.setTextColor(MessageTheme.getColor(
					mMessage.background_color_index, MessageTheme.COLOR_TEXT));

			refresh(mMessage);

			if (!mMessage.image_cid.isEmpty()) {
				WhateverApplication.getMainTaskManager().startTask(
						new ImageLoadTask(TASK_TAG, 0, mMessage.image_cid,
								"small", this));
				imageView_image.setOnClickListener(this);
			} else {
				imageView_image.setVisibility(View.GONE);
			}
		}
	}

	private void refresh(MessageData message) {
		btn_good.setNumber(message.good_count);
		btn_bad.setNumber(message.bad_count);
		btn_comment.setNumber(message.comment_count);

		textView_bad.setText(R.string.btn_message_bad);
		textView_good.setText(R.string.btn_message_good);
		imageView_bad.setImageResource(R.drawable.message_bad);
		imageView_good.setImageResource(R.drawable.message_good);
		if (message.put_bad) {
			imageView_bad.setImageResource(R.drawable.message_bad_already);
			textView_bad.setText(R.string.btn_message_bad_already);
		} else if (message.put_good) {
			imageView_good.setImageResource(R.drawable.message_good_already);
			textView_good.setText(R.string.btn_message_good_already);
		}
	}

	private void setupButtons() {
		if (mMessage.is_me) {
			button_bottom.setVisibility(View.GONE);
			button_bottom_1.setVisibility(View.GONE);
			button_bottom_2.setVisibility(View.GONE);
			button_bottom_3.setVisibility(View.GONE);
			bkg_normal
					.setBackgroundResource(R.drawable.message_bottom_background_t);
			bkg_trans
					.setBackgroundResource(R.drawable.message_bottom_background_trans_t);
		} else {
			button_bottom.setVisibility(View.VISIBLE);
			button_bottom_1.setVisibility(View.VISIBLE);
			button_bottom_2.setVisibility(View.VISIBLE);
			button_bottom_3.setVisibility(View.VISIBLE);
			bkg_normal
					.setBackgroundResource(R.drawable.message_bottom_background);
			bkg_trans
					.setBackgroundResource(R.drawable.message_bottom_background_trans);
		}

		btn_comment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MessageActivity.this, CommentActivity.class);
				intent.putExtra("message_cid", mMessage.cid);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
			}
		});

		btn_who.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog alertDialog = new MyAlertDialog.Builder(
						MessageActivity.this)
						.setTitle("WHO")
						.setIcon(R.drawable.message_who)
						.setMessage(
								"\u3000\u3000对方将收到你的信息（姓名、手机号），并有权决定是否与你交换信息，确定发送？")
						.setPositiveButton("确定",
								new MyAlertDialog.OnClickListener() {
									@Override
									public boolean onClick(
											DialogInterface dialog, int which) {

										WhateverApplication
												.getMainTaskManager()
												.startTask(
														new WHOTask(
																TASK_TAG,
																mMessage.cid,
																MessageActivity.this));
										Toast.makeText(MessageActivity.this,
												"发送WHO请求中", Toast.LENGTH_SHORT)
												.show();
										return true;
									}
								}).setNegativeButton("取消", null).create();
				alertDialog.show();
			}
		});

		btn_report.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View reportView = LayoutInflater.from(MessageActivity.this)
						.inflate(R.layout.dialog_report, null);
				final EditText editText_report = (EditText) reportView
						.findViewById(R.id.editText_report);
				Spinner spinner_default = (Spinner) reportView
						.findViewById(R.id.spinner_default);
				spinner_default
						.setOnItemSelectedListener(new OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent,
									View view, int position, long id) {
								String t = MessageActivity.this.getResources()
										.getStringArray(R.array.report_default)[position];
								editText_report.setText(t);
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}
						});

				AlertDialog alertDialog = new MyAlertDialog.Builder(
						MessageActivity.this)
						.setTitle("举报")
						.setView(reportView)
						.setIcon(R.drawable.message_report)
						.setPositiveButton("举报",
								new MyAlertDialog.OnClickListener() {
									@Override
									public boolean onClick(
											DialogInterface dialog, int which) {
										editText_report.setError(null);
										String content = editText_report
												.getText().toString().trim();
										String cid = mMessage.cid;
										if (content.isEmpty()) {
											editText_report
													.setError("举报内容不能为空");
											return false;
										}

										WhateverApplication
												.getMainTaskManager()
												.startTask(
														new ReportTask(
																"Global",
																cid,
																content,
																WhateverApplication
																		.getApplication()));

										return true;
									}
								}).setNegativeButton("算了吧", null).create();

				alertDialog.show();
			}
		});

		btn_bad.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WhateverApplication.getMainTaskManager()
						.startTask(
								new MessageMannerTask(TASK_TAG, mMessage.cid,
										mMessage.put_bad ? 0 : 1,
										MessageActivity.this));
			}
		});
		btn_good.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WhateverApplication.getMainTaskManager()
						.startTask(
								new MessageMannerTask(TASK_TAG, mMessage.cid,
										mMessage.put_good ? 0 : 2,
										MessageActivity.this));
			}
		});
		btn_pm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PMActivity.startPMSession(MessageActivity.this, mMessage.cid,
						null);
			}
		});
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
	}

	@Override
	protected void onDestroy() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);
		super.onDestroy();
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	private int verticalMinDistance = 50;
	private int minVelocity = 0;

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		float dx = e1.getX() - e2.getX(), dy = e1.getY() - e2.getY();

		if (dx > verticalMinDistance && Math.abs(velocityX) > minVelocity) {
			// 切换Activity
			// Intent intent = new Intent(ViewSnsActivity.this,
			// UpdateStatusActivity.class);
			// startActivity(intent);
			// Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
		} else if (-dx > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity
				&& Math.abs(dx) > Math.abs(dy)) {
			finish();
			return true;
			// Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mGestureDetector.onTouchEvent(ev)) {
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onImageLoaded(int taskCode, String cid, String size,
			Bitmap image) {
		if (mMessage != null) {
			if (cid == mMessage.image_cid) {
				if (image != null) {
					mMessage.image_prev = image;
					imageView_image.setImageBitmap(image);
					imageView_image.setBackgroundColor(0);
					imageView_image.setScaleType(ScaleType.FIT_CENTER);
				} else {
					imageView_image.setBackgroundColor(0xffcccccc);
					imageView_image.setImageResource(R.drawable.image_broken_n);
					imageView_image.setScaleType(ScaleType.CENTER);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent();
		i.setClass(this, ViewImagePopupActivity.class);
		i.putExtra("image_cid", mMessage.image_cid);
		startActivity(i);
	}

	@Override
	public void onMessageRefresh(int result, String cid, MessageData message) {
		if (mCid != cid && (mMessage != null && cid != mMessage.cid)) {
			return;
		}

		if (result == MessageRefreshTask.REFRESH_SUCCESS) {
			mMessage = message;
			displayMessage();
		} else if (result == MessageRefreshTask.REFRESH_FAIL_DELETED) {
			Toast.makeText(this, "这条状态已被删除", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void onMannerPut(int result, String messageId, int manner,
			int good_count, int bad_count) {
		if (mMessage == null || messageId != mMessage.cid)
			return;

		if (result == MessageMannerTask.PUT_SUCCESS) {
			switch (manner) {
			case 1:
				Toast.makeText(this, "你踩了一下~", Toast.LENGTH_SHORT).show();
				mMessage.put_bad = true;
				mMessage.put_good = false;
				break;
			case 2:
				Toast.makeText(this, "你赞了一下~", Toast.LENGTH_SHORT).show();
				mMessage.put_bad = false;
				mMessage.put_good = true;
				break;
			default:
				Toast.makeText(this, "你取消了评价~", Toast.LENGTH_SHORT).show();
				mMessage.put_bad = false;
				mMessage.put_good = false;
				break;
			}
			mMessage.good_count = good_count;
			mMessage.bad_count = bad_count;
			refresh(mMessage);
		} else if (result == MessageMannerTask.PUT_FAIL_ALREADY) {
			switch (manner) {
			case 1:
				Toast.makeText(this, "你已经踩过了哦~", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(this, "你已经赞过了哦~", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(this, "你还没有评价过呢~", Toast.LENGTH_SHORT).show();
				break;
			}
		} else {
			Toast.makeText(this, "啊哦好像失败了~", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onWHOAsked(int result, String cid) {
		if (mMessage.cid.equals(cid)) {
			switch (result) {
			case WHOTask.WHO_SUCCESS:
				Toast.makeText(MessageActivity.this, "请求发送成功",
						Toast.LENGTH_SHORT).show();
				break;
			case WHOTask.WHO_FAIL_ALREADY:
				Toast.makeText(MessageActivity.this, "不可以重复WHO哦~",
						Toast.LENGTH_SHORT).show();
				break;
			case WHOTask.WHO_FAIL_INNER_ERROR:
				Toast.makeText(MessageActivity.this, "失败啦~请稍后再试",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	@Override
	public void onWHOReplied(int result, String wid, boolean agree) {
	}

}
