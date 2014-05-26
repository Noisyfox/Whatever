package mynuaa.whatever;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import mynuaa.whatever.DataSource.NotificationData;
import mynuaa.whatever.DataSource.NotificationGetTask;
import mynuaa.whatever.DataSource.NotificationGetTask.OnNotificationGetListener;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class NotifyFragment extends SherlockFragment implements
		OnNotificationGetListener, OnRefreshListener<ListView>,
		OnItemClickListener {
	private static final String TASK_TAG = "task_notify_fragment";

	private NotifyAdapter notifyAdapter = new NotifyAdapter();// 自定义适配器
	private View loadingView;// 加载视图的布局
	private PullToRefreshListView rootListView;
	private ListView listView;
	boolean loading = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_notify, container,
				false);

		loadingView = inflater.inflate(R.layout.footer, null);
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_read);

		rootListView = (PullToRefreshListView) rootView
				.findViewById(R.id.main_list);
		listView = rootListView.getRefreshableView();

		// 添加底部加载视图
		listView.addFooterView(loadingView);
		// 初始化适配器
		rootListView.setAdapter(notifyAdapter);
		rootListView.setOnRefreshListener(this);

		notifyAdapter.mergeData(true, NotificationData.getUnreadNotification());

		listView.setOnItemClickListener(this);

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		updateCurrendData(true);

		return rootView;
	}

	@Override
	public void onDestroyView() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);

		super.onDestroyView();
	}

	static class ViewHolder {
		TextView tv_content;
		TextView tv_note;
		TextView tv_type;
		TriangleCornerView tcv_corner;
	}

	class NotifyAdapter extends BaseAdapter {

		private LinkedList<NotificationData> mData = new LinkedList<NotificationData>();

		public synchronized boolean mergeData(boolean atTop,
				List<NotificationData> data) {
			boolean isModified;
			if (atTop) {
				isModified = mData.removeAll(data);
				Stack<NotificationData> ts = new Stack<NotificationData>();
				ts.addAll(data);
				while (!ts.isEmpty()) {
					mData.addFirst(ts.pop());
					isModified = true;
				}
			} else {
				LinkedList<NotificationData> tl = new LinkedList<NotificationData>();
				tl.addAll(data);
				tl.removeAll(mData);
				isModified = mData.addAll(tl);
			}

			if (isModified) {
				for (NotificationData nd : mData) {
					switch (nd.type) {
					case NotificationData.TYPE_GOOD:
					case NotificationData.TYPE_BAD:
						NotificationData.setRead(nd.cid);
						break;
					}
				}
				notifyDataSetChanged();
			}
			return isModified;
		}

		public synchronized String getLastId() {
			return mData.size() == 0 ? null : mData.getLast().cid;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mData.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(
						NotifyFragment.this.getActivity()).inflate(
						R.layout.notify_list_item, arg2, false);
				holder = new ViewHolder();
				holder.tv_content = (TextView) convertView
						.findViewById(R.id.textView_content);
				holder.tv_type = (TextView) convertView
						.findViewById(R.id.textView_type);
				holder.tcv_corner = (TriangleCornerView) convertView
						.findViewById(R.id.imageView_corner);
				holder.tv_note = (TextView) convertView
						.findViewById(R.id.textView_note);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			NotificationData nd = mData.get(arg0);

			holder.tcv_corner.setVisibility(nd.isRead ? View.INVISIBLE
					: View.VISIBLE);
			holder.tcv_corner.setColor(MessageTheme.getColor(nd.theme_color,
					MessageTheme.COLOR_CORNER));
			holder.tv_content.setText("\"" + nd.content + "\"");
			Resources res = getActivity().getResources();
			switch (nd.type) {
			case NotificationData.TYPE_GOOD:
				holder.tv_type.setText(res.getString(R.string.notify_type_good,
						nd.count));
				holder.tv_note.setVisibility(View.GONE);
				break;
			case NotificationData.TYPE_BAD:
				holder.tv_type.setText(res.getString(R.string.notify_type_bad,
						nd.count));
				holder.tv_note.setVisibility(View.GONE);
				break;
			case NotificationData.TYPE_COMMENT:
				holder.tv_type.setText(res.getString(
						R.string.notify_type_comment, nd.count));
				holder.tv_note.setVisibility(View.VISIBLE);
				holder.tv_note.setText("\"" + nd.note + "\"");
				break;
			case NotificationData.TYPE_PM:
				holder.tv_type.setText(res.getString(R.string.notify_type_pm,
						nd.count));
				holder.tv_note.setVisibility(View.VISIBLE);
				holder.tv_note.setText("\"" + nd.note + "\"");
				break;
			case NotificationData.TYPE_REPORT:
				holder.tv_type.setText(res.getString(
						R.string.notify_type_report, nd.count));
				break;
			case NotificationData.TYPE_WHO:
				holder.tv_type.setText(res.getString(R.string.notify_type_who,
						nd.count));
				break;
			default:
			}

			return convertView;
		}

	}

	@Override
	public void onNotificationGet(int result, int type,
			List<NotificationData> nds) {
		if (result == NotificationGetTask.GET_SUCCESS) {
			if (type == NotificationGetTask.TYPE_UNREAD) {
				notifyAdapter.mergeData(true, nds);
				clearLoading();
			} else if (type == NotificationGetTask.TYPE_READ) {
				notifyAdapter.mergeData(false, nds);
				clearLoading();
			}
		}
	}

	private synchronized void clearLoading() {
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_read);
		// 进入下一页，此时视图未加载.

		rootListView.post(new Runnable() {
			@Override
			public void run() {
				rootListView.onRefreshComplete();
				loading = false;
			}
		});
	}

	private synchronized void updateCurrendData(boolean getNew) {
		if (loading) {
			return;
		}
		loading = true;

		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_loading);

		if (getNew) {
			WhateverApplication.getMainTaskManager().startTask(
					new NotificationGetTask(TASK_TAG, this));
		} else {
			WhateverApplication.getMainTaskManager().startTask(
					new NotificationGetTask(TASK_TAG,
							notifyAdapter.getLastId(), this));
		}
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		updateCurrendData(true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (view == loadingView) {
			updateCurrendData(false);
		} else {
			NotificationData nd = (NotificationData) parent.getAdapter()
					.getItem(position);

			nd.isRead = true;
			NotificationData.setRead(nd.cid);
			notifyAdapter.notifyDataSetChanged();

			switch (nd.type) {
			case NotificationData.TYPE_GOOD:
			case NotificationData.TYPE_BAD:
				MessageActivity.showMessage(getActivity(), nd.ncid);
				break;
			case NotificationData.TYPE_COMMENT: {
				Intent intent = new Intent();
				Activity activity = getActivity();
				intent.setClass(activity, CommentActivity.class);

				intent.putExtra("message_cid", nd.ncid);
				intent.putExtra("return_message", true);

				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.slide_in_right,
						R.anim.stay);
				break;
			}
			case NotificationData.TYPE_PM:
				PMActivity.startPMSession(getActivity(), null, nd.pmsession);
				break;
			case NotificationData.TYPE_REPORT:
				break;
			case NotificationData.TYPE_WHO:
				break;
			}
		}
	}

}
