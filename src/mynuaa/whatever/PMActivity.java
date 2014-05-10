package mynuaa.whatever;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import mynuaa.whatever.DataSource.MessageData;
import mynuaa.whatever.DataSource.PMData;
import mynuaa.whatever.DataSource.PMGetTask;
import mynuaa.whatever.DataSource.PMGetTask.OnPMGetListener;
import mynuaa.whatever.DataSource.PMPostTask;
import mynuaa.whatever.DataSource.PMPostTask.OnPMPostListener;
import mynuaa.whatever.DataSource.PMRequireSessionTask;
import mynuaa.whatever.DataSource.PMRequireSessionTask.OnSessionGetListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class PMActivity extends SherlockFragmentActivity implements
		OnGestureListener, OnClickListener, OnRefreshListener<ListView>,
		OnLongClickListener, OnSessionGetListener, OnPMPostListener,
		OnPMGetListener, EmojiconGridFragment.OnEmojiconClickedListener,
		EmojiconsFragment.OnEmojiconBackspaceClickedListener, OnTouchListener,
		OnFocusChangeListener {
	private static final String TASK_TAG = "task_pm_activity";

	public static void startPMSession(Activity activity, String cid,
			String session) {
		Intent i = new Intent();
		i.setClass(activity, PMActivity.class);
		if (cid != null) {
			i.putExtra("cid", cid);
		} else {
			i.putExtra("session", session);
		}
		activity.startActivity(i);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
	}

	GestureDetector mGestureDetector;

	private PullToRefreshListView rootListView;
	private ListView listView;
	private EditText editText_message;
	private View button_send, button_emoji;
	private View loadingView;

	private Fragment fragment_emoji;

	private LinkedList<PMData> currentData = new LinkedList<PMData>();
	private PMAdapter currentAdapter = new PMAdapter();
	boolean loading = false;

	private String cid;
	private String session;

	private final static long DIVIDER_TIME = 3 * 60 * 1000;

	private void mergeData(boolean newMessage, List<PMData> pmData) {
		if (newMessage) {
			pmData.remove(currentData);
			long lastTime = currentData.isEmpty() ? 0
					: currentData.getLast().timeL;
			lastTime += DIVIDER_TIME;

			for (PMData pd : pmData) {
				long t = pd.timeL;
				if (t > lastTime) {
					currentData.add(PMData.createTimeDivider(cid, t));
				}
				currentData.add(pd);
				lastTime = t + DIVIDER_TIME;
			}
		} else {
			currentData.remove(pmData);
			long lastTime = currentData.isEmpty() ? -1
					: currentData.getFirst().timeL;
			Stack<PMData> pms = new Stack<PMData>();
			for (PMData pd : pmData) {
				pms.push(pd);
			}
			boolean added = false;
			while (!pms.isEmpty()) {
				added = true;
				PMData pd = pms.pop();
				if (pd.timeL + DIVIDER_TIME > lastTime) {// 需要合并
					if (!currentData.isEmpty()
							&& currentData.getFirst().from == PMData.FROM_DIVIDER) {
						currentData.removeFirst();
					}
				} else {
					if (currentData.isEmpty()
							|| currentData.getFirst().from != PMData.FROM_DIVIDER) {
						currentData.addFirst(PMData.createTimeDivider(cid,
								lastTime));
					}
				}
				currentData.addFirst(pd);
				lastTime = pd.timeL;
			}
			if (added) {
				currentData.addFirst(PMData.createTimeDivider(cid, lastTime));
			}
		}
	}

	private static class ViewHolder {
		TextView tv;
		View loadingView;
	}

	private class PMAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return currentData.size();
		}

		@Override
		public Object getItem(int position) {
			return currentData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return currentData.get(position).from;
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PMData pmd = currentData.get(position);

			ViewHolder viewHolder;

			switch (pmd.from) {
			case PMData.FROM_ME: {
				if (convertView == null) {
					convertView = LayoutInflater.from(PMActivity.this).inflate(
							R.layout.pm_list_item_message_me, parent, false);
					viewHolder = new ViewHolder();
					viewHolder.tv = (TextView) convertView
							.findViewById(R.id.textView_message);
					viewHolder.loadingView = convertView
							.findViewById(R.id.progressBar_loading);
					convertView.setTag(R.id.tag_pmViewHolder, viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView
							.getTag(R.id.tag_pmViewHolder);
				}
				viewHolder.tv.setText(pmd.content);
				viewHolder.tv.setOnLongClickListener(PMActivity.this);
				switch (pmd.status) {
				case PMData.STATUS_NORMAL:
					viewHolder.loadingView.setVisibility(View.GONE);
					break;
				case PMData.STATUS_SENDING:
					viewHolder.loadingView.setVisibility(View.VISIBLE);
					break;
				case PMData.STATUS_FAIL:
					viewHolder.loadingView.setVisibility(View.VISIBLE);
					break;
				}

				break;
			}
			case PMData.FROM_OTHER: {
				if (convertView == null) {
					convertView = LayoutInflater.from(PMActivity.this).inflate(
							R.layout.pm_list_item_message_other, parent, false);
					viewHolder = new ViewHolder();
					viewHolder.tv = (TextView) convertView
							.findViewById(R.id.textView_message);
					convertView.setTag(R.id.tag_pmViewHolder, viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView
							.getTag(R.id.tag_pmViewHolder);
				}
				viewHolder.tv.setText(pmd.content);
				viewHolder.tv.setOnLongClickListener(PMActivity.this);
				break;
			}
			case PMData.FROM_DIVIDER: {
				if (convertView == null) {
					convertView = LayoutInflater.from(PMActivity.this).inflate(
							R.layout.pm_list_item_time, parent, false);
					viewHolder = new ViewHolder();
					viewHolder.tv = (TextView) convertView
							.findViewById(R.id.textView_time);
					convertView.setTag(R.id.tag_pmViewHolder, viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView
							.getTag(R.id.tag_pmViewHolder);
				}
				viewHolder.tv.setText(pmd.time);
				break;
			}
			}

			return convertView;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pm);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		rootListView = (PullToRefreshListView) findViewById(R.id.listView_messages);
		listView = rootListView.getRefreshableView();
		editText_message = (EditText) findViewById(R.id.editText_message);
		button_send = findViewById(R.id.button_send);
		button_emoji = findViewById(R.id.button_emoji);
		loadingView = findViewById(R.id.pm_load_process);

		FragmentManager fm = getSupportFragmentManager();
		fragment_emoji = fm.findFragmentById(R.id.fragment_emoji);

		listView.setOnTouchListener(this);

		ILoadingLayout ll = rootListView.getLoadingLayoutProxy();
		ll.setPullLabel(getText(R.string.pm_refresh_pulling));
		ll.setReleaseLabel(getText(R.string.pm_refresh_release));

		// Util.setupCommonActionBar(this, R.string.pm_title);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.pm_title);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		mGestureDetector = new GestureDetector(this, this);

		cid = getIntent().getStringExtra("cid");
		session = getIntent().getStringExtra("session");

		loadingView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		if (session == null) {
			WhateverApplication.getMainTaskManager().startTask(
					new PMRequireSessionTask(TASK_TAG, cid, this));
		} else {
			WhateverApplication.getMainTaskManager().startTask(
					new PMGetTask(TASK_TAG, session, this));
		}
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

	private void finishLoad(List<PMData> unread) {
		if (unread == null) {
			mergeData(true, PMData.getCachedMessages(session, -1));// 载入缓存
		} else {
			mergeData(true, unread);// 载入未读消息
		}

		rootListView.setAdapter(currentAdapter);
		rootListView.setOnRefreshListener(this);

		button_send.setOnClickListener(this);
		button_emoji.setOnClickListener(this);
		editText_message.setOnClickListener(this);
		editText_message.setOnFocusChangeListener(this);
		editText_message.setEnabled(true);
		button_send.setEnabled(true);
		loadingView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		if (v == button_send) {
			String message = editText_message.getText().toString();
			editText_message.setText("");
			if (message.isEmpty()) {
				return;
			}

			PMData pd = new PMData();
			pd.content = message;
			pd.timeL = System.currentTimeMillis();
			pd.ncid = cid;
			pd.cid = -1;
			pd.from = PMData.FROM_ME;
			pd.session = session;
			pd.status = PMData.STATUS_SENDING;

			LinkedList<PMData> l = new LinkedList<PMData>();
			l.add(pd);
			mergeData(true, l);
			currentAdapter.notifyDataSetChanged();

			WhateverApplication.getMainTaskManager().startTask(
					new PMPostTask(TASK_TAG, pd, this));
		} else if (v == button_emoji) {
			Util.setInputMethod(this, false);

			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, true);
		} else if (v == editText_message) {
			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, false);
		}
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
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
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
	public void onRefresh(final PullToRefreshBase<ListView> refreshView) {
		if (loading) {
			return;
		}
		loading = true;
		refreshView.post(new Runnable() {
			@Override
			public void run() {
				synchronized (currentData) {
					int before = currentData.size() < 2 ? -1 : currentData
							.get(1).cid;
					Log.d("ssss", before + "");
					mergeData(false, PMData.getCachedMessages(session, before));
					currentAdapter.notifyDataSetChanged();
				}
				refreshView.onRefreshComplete();
				loading = false;
			}
		});
	}

	@Override
	public boolean onLongClick(View v) {
		if (!TextView.class.isInstance(v)) {
			return false;
		}

		TextView tv = (TextView) v;

		String s = tv.getText().toString();
		ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		cbm.setText(s);

		Toast.makeText(this, R.string.pm_message_copyed, Toast.LENGTH_SHORT)
				.show();

		return true;
	}

	@Override
	public void onSessionGet(int result, String ncid, String session) {
		if (ncid.equals(cid)) {
			if (result == PMRequireSessionTask.GET_SUCCESS) {
				this.session = session;
				WhateverApplication.getMainTaskManager().startTask(
						new PMGetTask(TASK_TAG, session, this));
			} else if (result == PMRequireSessionTask.GET_FAIL_SELF) {
				Toast.makeText(this, "不可以自己私信给自己哦~", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(this, "会话创建失败，请稍后再试~", Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.pmactivity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();

			if (getIntent().hasExtra("message")) {
				Intent intent = new Intent();
				MessageData md = getIntent().getParcelableExtra("message");
				intent.putExtra("message", md);
				intent.setClass(this, MessageActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
			}
			return true;
		case R.id.pmhistory: {
			PMHistoryActivity.showHistory(this, session);
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPMPost(int result, PMData pmData) {
		if (result == PMPostTask.POST_SUCCESS) {
			pmData.status = PMData.STATUS_NORMAL;
		} else {
			pmData.status = PMData.STATUS_FAIL;
		}
		PMData.savePMCache(pmData);
		currentAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPMGet(int result, int type, List<PMData> pmData) {
		if (result == PMGetTask.GET_SUCCESS) {
			if (type == PMGetTask.TYPE_UNREAD) {
				if (pmData.isEmpty()) {
					finishLoad(null);
				} else {
					finishLoad(pmData);
				}
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}
		return false;
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(editText_message);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(editText_message, emojicon);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v == editText_message && hasFocus) {
			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, false);
		}
	}
}
