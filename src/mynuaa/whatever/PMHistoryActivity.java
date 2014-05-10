package mynuaa.whatever;

import java.util.ArrayList;
import java.util.List;

import mynuaa.whatever.DataSource.PMData;
import mynuaa.whatever.DataSource.PMGetTask;
import mynuaa.whatever.DataSource.PMData.PMHistory;
import mynuaa.whatever.DataSource.PMGetTask.OnPMGetListener;
import mynuaa.whatever.DataSource.PMHistoryDeleteTask;
import mynuaa.whatever.DataSource.PMHistoryDeleteTask.OnHistoryDeleteListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PMHistoryActivity extends SherlockActivity implements
		OnGestureListener, TextWatcher, OnClickListener, OnPMGetListener,
		OnHistoryDeleteListener {
	private static final String TASK_TAG = "task_pm_history_activity";
	private static final int HISTORY_PER_PAGE = 8;

	GestureDetector mGestureDetector;

	public static void showHistory(Activity activity, String session) {
		Intent i = new Intent();
		i.setClass(activity, PMHistoryActivity.class);
		i.putExtra("session", session);
		activity.startActivity(i);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
	}

	private static class ViewHolder {
		TextView tv_from, tv_time, tv_content;
	}

	private class HistoryAdapter extends BaseAdapter {

		private ArrayList<PMData> mData = new ArrayList<PMData>();

		public void setData(List<PMData> data) {
			mData.clear();
			mData.addAll(data);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			if (convertView == null) {
				convertView = LayoutInflater.from(PMHistoryActivity.this)
						.inflate(R.layout.pm_list_item_history, parent, false);
				vh = new ViewHolder();
				convertView.setTag(vh);
				vh.tv_from = (TextView) convertView
						.findViewById(R.id.textView_from);
				vh.tv_time = (TextView) convertView
						.findViewById(R.id.textView_time);
				vh.tv_content = (TextView) convertView
						.findViewById(R.id.textView_content);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}

			PMData pd = mData.get(position);
			vh.tv_content.setText(pd.content);
			vh.tv_from
					.setText(pd.from == PMData.FROM_ME ? R.string.pm_history_from_me
							: R.string.pm_history_from_other);
			vh.tv_time.setText(Util.getLeveledTime(pd.timeL));

			return convertView;
		}

	}

	private String session;
	private PMHistory history;
	private int totalPage = 1;
	private int currentPage = 1;

	private ListView listView_history;
	private EditText editText_currPage;
	private TextView textView_totalPage;
	private View btn_prev, btn_next, btn_delete;

	private ProgressDialog mProgressDialog = null;

	private HistoryAdapter mAdapter = new HistoryAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pmhistory);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.pm_history_title);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		mGestureDetector = new GestureDetector(this, this);

		session = getIntent().getStringExtra("session");

		if (session == null) {
			finish();
			return;
		}

		listView_history = (ListView) findViewById(R.id.listView_history);
		editText_currPage = (EditText) findViewById(R.id.editText_currPage);
		textView_totalPage = (TextView) findViewById(R.id.textView_totalPage);
		btn_prev = findViewById(R.id.btn_prev);
		btn_next = findViewById(R.id.btn_next);
		btn_delete = findViewById(R.id.button_delete);

		listView_history.setAdapter(mAdapter);

		editText_currPage.addTextChangedListener(this);
		btn_prev.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_delete.setOnClickListener(this);

		reloadHistory();

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
	}

	private void reloadHistory() {
		history = new PMHistory(session);
		totalPage = history.length();
		totalPage = totalPage / HISTORY_PER_PAGE
				+ (totalPage % HISTORY_PER_PAGE == 0 ? 0 : 1);
		if (totalPage == 0) {
			totalPage = 1;
		}

		String totalStr = String.valueOf(totalPage);
		textView_totalPage.setText(totalStr);
		editText_currPage.setText(totalStr);
		editText_currPage.setEms(totalStr.length());

		loadPage(totalPage);
	}

	private void loadPage(int page) {
		if (page > totalPage) {
			page = totalPage;
		}
		if (page < 1) {
			page = 1;
		}
		currentPage = page;
		mAdapter.setData(history.loadPage(page, HISTORY_PER_PAGE));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.pmhistory, menu);
		return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.pm_history_sync: {
			Dialog alertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.pm_history_sync_ask_title)
					.setMessage(R.string.pm_history_sync_ask)
					.setPositiveButton(R.string.button_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mProgressDialog = ProgressDialog.show(
											PMHistoryActivity.this, null,
											"正在同步", false);

									WhateverApplication
											.getMainTaskManager()
											.startTask(
													new PMGetTask(
															TASK_TAG,
															session,
															PMGetTask.TYPE_NORMAL,
															PMHistoryActivity.this));
								}
							}).setNegativeButton(R.string.button_cancel, null)
					.create();
			alertDialog.show();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
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
		if (mGestureDetector.onTouchEvent(ev)) {
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		String vs = s.toString();
		if (!vs.isEmpty()) {
			loadPage(Integer.parseInt(vs));
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btn_delete) {
			Dialog alertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.pm_history_delete_ask_title)
					.setMessage(R.string.pm_history_delete_ask)
					.setPositiveButton(R.string.button_delete,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mProgressDialog = ProgressDialog.show(
											PMHistoryActivity.this, null,
											"正在删除", false);

									WhateverApplication
											.getMainTaskManager()
											.startTask(
													new PMHistoryDeleteTask(
															TASK_TAG,
															session,
															PMHistoryActivity.this));
								}
							}).setNegativeButton(R.string.button_cancel, null)
					.create();
			alertDialog.show();
			return;
		} else if (v == btn_prev) {
			currentPage--;
		} else if (v == btn_next) {
			currentPage++;
		}
		loadPage(currentPage);
		editText_currPage.setText(String.valueOf(currentPage));
	}

	@Override
	public void onPMGet(int result, int type, List<PMData> pmData) {
		if (mProgressDialog != null) {
			mProgressDialog.cancel();
			mProgressDialog = null;
		}
		if (result == PMGetTask.GET_SUCCESS) {
			if (type == PMGetTask.TYPE_NORMAL) {
				reloadHistory();
				return;
			}
		}
		Toast.makeText(this, "啊哦好像出错了呢~", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onHistoryDelete(String session) {
		if (mProgressDialog != null) {
			mProgressDialog.cancel();
			mProgressDialog = null;
		}
		reloadHistory();
	}

}
