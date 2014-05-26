package mynuaa.whatever;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import mynuaa.whatever.DataSource.CommentData;
import mynuaa.whatever.DataSource.CommentGetTask;
import mynuaa.whatever.DataSource.CommentGetTask.OnCommentGetListener;
import mynuaa.whatever.DataSource.CommentPostTask;
import mynuaa.whatever.DataSource.CommentPostTask.OnCommentPostListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CommentActivity extends SherlockFragmentActivity implements
		OnItemClickListener, InputFilter, OnClickListener,
		OnCommentPostListener, OnCommentGetListener, OnGestureListener,
		EmojiconGridFragment.OnEmojiconClickedListener,
		EmojiconsFragment.OnEmojiconBackspaceClickedListener, OnTouchListener,
		OnFocusChangeListener {
	private static final String TASK_TAG = "task_comment_activity";

	GestureDetector mGestureDetector;

	private final List<CommentData> currentData = new LinkedList<CommentData>();

	private ListView listView_comment;
	private EditText editText_comment;
	private View button_send, button_emoji;
	private View loadingView;// 加载视图的布局

	private Fragment fragment_emoji;

	private CommentAdapter mCommentAdapter = new CommentAdapter();

	private String mMessage_cid;
	private boolean mReturn_message;

	private int mReplyTo = -1;
	private String mReplyToPrefix = "";
	private boolean isLoading = false;// 是否加载过,控制加载次数

	private String mLastMergedCommentCid = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mGestureDetector = new GestureDetector(this, this);
		isLoading = false;

		mMessage_cid = getIntent().getStringExtra("message_cid");
		mReturn_message = getIntent().getBooleanExtra("return_message", false);

		listView_comment = (ListView) findViewById(R.id.listView_comment);
		editText_comment = (EditText) findViewById(R.id.editText_comment);
		button_send = findViewById(R.id.button_send);
		button_emoji = findViewById(R.id.button_emoji);

		FragmentManager fm = getSupportFragmentManager();
		fragment_emoji = fm.findFragmentById(R.id.fragment_emoji);

		loadingView = LayoutInflater.from(this).inflate(R.layout.footer, null);
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_more);

		Util.hideHintOnFocused(editText_comment);
		Util.setupCommonActionBar(this, R.string.comment_title);

		if (mReturn_message) {
			Button btn_send = (Button) getSupportActionBar().getCustomView()
					.findViewById(R.id.right_btn);
			btn_send.setVisibility(View.VISIBLE);
			btn_send.setText(R.string.btn_back_to_message);
			btn_send.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();

					MessageActivity.showMessage(CommentActivity.this,
							mMessage_cid);
				}
			});
		}

		listView_comment.addFooterView(loadingView);
		listView_comment.setAdapter(mCommentAdapter);
		listView_comment.setOnItemClickListener(this);
		listView_comment.setOnTouchListener(this);

		editText_comment.setFilters(new InputFilter[] { this });
		editText_comment.setOnFocusChangeListener(this);
		editText_comment.setOnClickListener(this);

		button_send.setOnClickListener(this);
		button_emoji.setOnClickListener(this);

		loadingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateCurrendData(false);
			}
		});

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
		updateCurrendData(true);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (fragment_emoji.isVisible()) {
				FragmentManager fm = getSupportFragmentManager();
				Util.toggleEmojiFragment(fm, fragment_emoji, false);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		FragmentManager fm = getSupportFragmentManager();
		Util.toggleEmojiFragment(fm, fragment_emoji, false);
	}

	@Override
	protected void onDestroy() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);

		super.onDestroy();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
	}

	private void updateCurrendData(boolean getNew) {
		if (isLoading) {
			return;
		}

		isLoading = true;
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_loading);

		String prev = (getNew || mLastMergedCommentCid.isEmpty()) ? null
				: mLastMergedCommentCid;
		/*
		 * if (!getNew) { synchronized (currentData) { if
		 * (!currentData.isEmpty()) { CommentData md =
		 * currentData.get(currentData.size() - 1); if (md != null) prev =
		 * md.cid; } } }
		 */

		WhateverApplication.getMainTaskManager().startTask(
				new CommentGetTask(TASK_TAG, mMessage_cid, prev, this));
	}

	class CommentAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			synchronized (currentData) {
				return currentData.size();
			}
		}

		@Override
		public Object getItem(int position) {
			synchronized (currentData) {
				return currentData.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (arg1 == null)
				arg1 = LayoutInflater.from(CommentActivity.this).inflate(
						R.layout.comment_list_item, null);

			TextView textView_user = (TextView) arg1
					.findViewById(R.id.textView_user);
			TextView textView_time = (TextView) arg1
					.findViewById(R.id.textView_time);
			TextView textView_comment = (TextView) arg1
					.findViewById(R.id.textView_comment);

			CommentData md = (CommentData) getItem(arg0);

			textView_user.setText(md.user_display);
			textView_time.setText(md.time);
			textView_comment.setText(md.content);

			if (md.user == mReplyTo) {
				arg1.setBackgroundResource(R.color.comment_item_background_selected);
			} else {
				if (arg0 % 2 == 0) {
					arg1.setBackgroundResource(R.color.comment_item_background_0);
				} else {
					arg1.setBackgroundResource(R.color.comment_item_background_1);
				}
			}

			return arg1;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CommentData md = (CommentData) parent.getAdapter().getItem(position);
		selectReplyTo(md.user);
		mCommentAdapter.notifyDataSetChanged();
	}

	private void selectReplyTo(int replyTo) {
		if (!editText_comment.isEnabled()) {
			return;
		}

		String origComment = getComment();
		mReplyToPrefix = "";

		if (replyTo >= 0) {
			String pref = "回复 " + (replyTo == 0 ? "楼主" : "用户" + replyTo) + " :";
			editText_comment.setText(pref + origComment);
			mReplyToPrefix = pref;
		} else {
			editText_comment.setText(origComment);
		}
		mReplyTo = replyTo;
	}

	private void mergeComment(String prevId, List<CommentData> comments) {
		boolean addNew = false;

		if (comments != null && !comments.isEmpty()) {
			synchronized (currentData) {
				if (prevId == null) {// 在头部追加
					String lastCid = currentData.isEmpty() ? "" : currentData
							.get(0).cid;
					Stack<CommentData> cs = new Stack<CommentData>();
					for (CommentData cd : comments) {
						if (cd.cid.equals(lastCid)) {
							break;
						}
						cs.push(cd);
					}
					while (!cs.isEmpty()) {
						addNew = true;
						currentData.add(0, cs.pop());
					}
				} else {// 在尾部追加
					String lastCid = comments.get(0).cid;
					Iterator<CommentData> it = currentData.iterator();
					boolean needDelete = false;
					while (it.hasNext()) {
						if (needDelete) {
							it.next();
							it.remove();
						} else {
							CommentData cd = it.next();
							if (cd.cid.equals(lastCid)) {
								it.remove();
								needDelete = true;
							}
						}
					}
					for (CommentData cd : comments) {
						addNew = true;
						currentData.add(currentData.size(), cd);
						mLastMergedCommentCid = cd.cid;
					}
				}
				if (mLastMergedCommentCid.isEmpty()) {
					if (!currentData.isEmpty()) {
						CommentData md = currentData
								.get(currentData.size() - 1);
						if (md != null)
							mLastMergedCommentCid = md.cid;
					}
				}
			}
		}

		if (!addNew) {
			Toast.makeText(this, "没有更多评论了", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == button_send) {
			String comment = getComment();

			if (comment.isEmpty()) {
				Toast.makeText(this, "评论不能为空", Toast.LENGTH_SHORT).show();
				return;
			}
			editText_comment.setEnabled(false);
			button_send.setEnabled(false);

			CommentData cd = new CommentData();
			cd.content = comment;
			cd.replyTo = mReplyTo == -1 ? 0 : mReplyTo;
			cd.message_cid = mMessage_cid;

			WhateverApplication.getMainTaskManager().startTask(
					new CommentPostTask(TASK_TAG, cd, this));
		} else if (v == button_emoji) {
			Util.setInputMethod(this, false);

			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, true);
		} else if (v == editText_comment) {
			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, false);
		}
	}

	private String getComment() {
		String s = editText_comment.getEditableText().toString();

		s = s.substring(mReplyToPrefix.length()).trim();

		return s;
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {
		int prefixLen = mReplyToPrefix.length();

		if (dstart < prefixLen
				&& !(source.toString().equals(mReplyToPrefix) && dstart == 0)) {// 改了前缀
			selectReplyTo(-1);

			return "";
		}

		return null;
	}

	@Override
	public void onCommentPost(int result, CommentData comment) {
		editText_comment.setEnabled(true);
		button_send.setEnabled(true);

		if (result == CommentPostTask.POST_SUCCESS) {
			Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
			selectReplyTo(-1);
			editText_comment.setText("");

			synchronized (currentData) {
				currentData.add(comment);
			}
			mCommentAdapter.notifyDataSetChanged();
		} else {
			Toast.makeText(this, "评论失败", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onCommentGet(int result, String message_cid, String prevId,
			List<CommentData> comments) {
		if (result == CommentGetTask.GET_SUCCESS) {
			mergeComment(prevId, comments);
			mCommentAdapter.notifyDataSetChanged();
		}
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_more);
		isLoading = false;
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
		if (e1 == null) {
			return false;
		}
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
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(editText_comment);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(editText_comment, emojicon);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v == editText_comment && hasFocus) {
			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, false);
		}
	}

}
