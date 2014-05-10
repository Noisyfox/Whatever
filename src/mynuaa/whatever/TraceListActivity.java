package mynuaa.whatever;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import mynuaa.whatever.DataSource.MessageData;
import mynuaa.whatever.DataSource.TraceGetTask;
import mynuaa.whatever.DataSource.TraceGetTask.OnTraceGetListener;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TraceListActivity extends SherlockActivity implements
		OnGestureListener, OnTraceGetListener, OnItemClickListener {
	private static final String TASK_TAG = "task_trace_list_activity";

	private final List<MessageData> currentData = new LinkedList<MessageData>();

	private TraceAdapter mTraceAdapter = new TraceAdapter();

	GestureDetector mGestureDetector;
	private int mTraceFilter;

	ListView listView_trace;
	private View loadingView;// 加载视图的布局
	private boolean isLoading = false;// 是否加载过,控制加载次数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trace_list);

		listView_trace = (ListView) findViewById(R.id.listView_trace);

		loadingView = LayoutInflater.from(this).inflate(R.layout.footer, null);
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_more);

		mTraceFilter = this.getIntent().getIntExtra("trace",
				TraceGetTask.FILTER_SEND);

		mGestureDetector = new GestureDetector(this, this);

		switch (mTraceFilter) {
		case TraceGetTask.FILTER_BAD:
			Util.setupCommonActionBar(this, R.string.path_bad);
			break;
		case TraceGetTask.FILTER_GOOD:
			Util.setupCommonActionBar(this, R.string.path_good);
			break;
		case TraceGetTask.FILTER_COMMENT:
			Util.setupCommonActionBar(this, R.string.path_comment);
			break;
		case TraceGetTask.FILTER_SEND:
			Util.setupCommonActionBar(this, R.string.path_message);
			break;
		case TraceGetTask.FILTER_REPORT:
			Util.setupCommonActionBar(this, R.string.path_report);
			break;
		case TraceGetTask.FILTER_PM:
			Util.setupCommonActionBar(this, R.string.path_pm);
			break;
		default:
		}

		listView_trace.addFooterView(loadingView);
		listView_trace.setAdapter(mTraceAdapter);
		listView_trace.setOnItemClickListener(this);
		loadingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateCurrendData(false);
			}
		});

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		updateCurrendData(true);
		// WhateverApplication.getMainTaskManager().startTask(
		// new TraceGetTask(TASK_TAG, mTraceFilter, null, this));
	}

	private void updateCurrendData(boolean getNew) {
		if (isLoading) {
			return;
		}

		isLoading = true;
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_loading);

		String sel = null;
		if (!getNew) {
			synchronized (currentData) {
				if (!currentData.isEmpty()) {
					MessageData md = currentData.get(currentData.size() - 1);
					if (md != null)
						sel = md.select_id;
				}
			}
		}
		WhateverApplication.getMainTaskManager().startTask(
				new TraceGetTask(TASK_TAG, mTraceFilter, sel, this));
	}

	private void mergeMessage(String selectId, List<MessageData> messages) {
		boolean addNew = false;

		if (messages != null && !messages.isEmpty()) {
			synchronized (currentData) {
				if (selectId == null) {// 在头部追加
					String lastCid = currentData.isEmpty() ? "" : currentData
							.get(0).cid;
					Stack<MessageData> ms = new Stack<MessageData>();
					for (MessageData md : messages) {
						if (md.cid.equals(lastCid)) {
							break;
						}
						ms.push(md);
					}
					while (!ms.isEmpty()) {
						addNew = true;
						currentData.add(0, ms.pop());
					}
				} else {// 在尾部追加
					String lastCid = messages.get(0).cid;
					Iterator<MessageData> it = currentData.iterator();
					boolean needDelete = false;
					while (it.hasNext()) {
						if (needDelete) {
							it.next();
							it.remove();
						} else {
							MessageData md = it.next();
							if (md.cid.equals(lastCid)) {
								it.remove();
								needDelete = true;
							}
						}
					}
					for (MessageData md : messages) {
						addNew = true;
						currentData.add(currentData.size(), md);
					}
				}
			}
		}

		if (!addNew) {
			Toast.makeText(this, "没有更多状态了", Toast.LENGTH_SHORT).show();
		}
	}

	class TraceAdapter extends BaseAdapter {

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
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			if (convertView == null)
				convertView = LayoutInflater.from(TraceListActivity.this)
						.inflate(R.layout.trace_list_item, null);

			TextView textView_context = (TextView) convertView
					.findViewById(R.id.textView_content);
			TextView textView_time = (TextView) convertView
					.findViewById(R.id.textView_time);
			TriangleCornerView triangleCornerView_corner = (TriangleCornerView) convertView
					.findViewById(R.id.TriangleCornerView_corner);

			MessageData md = (MessageData) getItem(arg0);

			textView_context.setText(md.content);
			textView_time.setText(md.time);
			triangleCornerView_corner.setColor(MessageTheme.getColor(
					md.background_color_index, MessageTheme.COLOR_CORNER));

			return convertView;
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
	public void onTraceGet(int result, int filter, String selectId,
			List<MessageData> messages) {
		if (result == TraceGetTask.GET_SUCCESS && filter == mTraceFilter) {
			mergeMessage(selectId, messages);
			mTraceAdapter.notifyDataSetChanged();
			((TextView) loadingView.findViewById(R.id.textView_footer))
					.setText(R.string.footer_more);
			isLoading = false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (view == this.loadingView) {
			updateCurrendData(false);
		} else {
			Intent intent = new Intent();

			MessageData md = (MessageData) parent.getAdapter()
					.getItem(position);
			intent.putExtra("message", md);

			if (mTraceFilter == TraceGetTask.FILTER_PM) {
				intent.setClass(this, PMActivity.class);
				intent.putExtra("cid", md.cid);
			} else {
				intent.setClass(this, MessageActivity.class);
			}

			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
		}
	}

}
