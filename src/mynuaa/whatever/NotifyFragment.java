package mynuaa.whatever;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import mynuaa.whatever.DataSource.ContactSyncTask;
import mynuaa.whatever.DataSource.NotificationData;
import mynuaa.whatever.DataSource.NotificationGetTask;
import mynuaa.whatever.DataSource.UserSession;
import mynuaa.whatever.DataSource.NotificationGetTask.OnNotificationGetListener;
import mynuaa.whatever.DataSource.WHOTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
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

		setHasOptionsMenu(true);

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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.notipage, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.mark_all_read) {
			notifyAdapter.markAllRead();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyView() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);

		super.onDestroyView();
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

		public synchronized void markAllRead() {
			for (NotificationData nd : mData) {
				if (!nd.isRead) {
					nd.isRead = true;
					NotificationData.setRead(nd.cid);
				}
			}
			notifyDataSetChanged();
			((MainActivity) getActivity()).clearNotification();
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

		private class ViewHolder {
			TextView tv_content;
			TextView tv_note;
			TextView tv_type;
			TriangleCornerView tcv_corner;
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
				holder.tv_note.setVisibility(View.GONE);
				break;
			case NotificationData.TYPE_WHO: {
				String userName;
				try {
					JSONTokener jsonParser = new JSONTokener(nd.ext);

					jsonParser.nextTo('{');
					if (!jsonParser.more()) {
						throw new JSONException("Failed to read return value.");
					}
					JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

					userName = jsonObj.getString("username");
				} catch (JSONException e) {
					e.printStackTrace();
					break;
				}

				holder.tv_type.setText(Html.fromHtml(res.getString(
						R.string.notify_type_who, userName)));
				holder.tv_note.setVisibility(View.GONE);
				break;
			}
			case NotificationData.TYPE_WHO_REPLY:
				holder.tv_type.setText(res
						.getString(R.string.notify_type_who_reply));
				holder.tv_note.setVisibility(View.VISIBLE);
				holder.tv_note.setText("\"" + nd.note + "\"");
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
				UserSession session = UserSession.getCurrentSession();
				session.clearUnreadNotification();
				session.saveAsLocalSession(getActivity());

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
			final NotificationData nd = (NotificationData) parent.getAdapter()
					.getItem(position);

			nd.isRead = true;
			NotificationData.setRead(nd.cid);
			notifyAdapter.notifyDataSetChanged();

			((MainActivity) getActivity()).clearNotification();

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
			case NotificationData.TYPE_WHO: {
				String phone, realName, userName;
				try {
					JSONTokener jsonParser = new JSONTokener(nd.ext);

					jsonParser.nextTo('{');
					if (!jsonParser.more()) {
						throw new JSONException("Failed to read return value.");
					}
					JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

					phone = jsonObj.getString("phone");
					realName = jsonObj.getString("real_name");
					userName = jsonObj.getString("username");
				} catch (JSONException e) {
					e.printStackTrace();
					break;
				}

				String contactName = ContactSyncTask
						.contactEnabled(getActivity()) ? Util
						.findContactNameByNumber(getActivity(), phone) : null;

				StringBuilder sb = new StringBuilder();
				sb.append(userName);
				sb.append(',');
				sb.append(realName);
				sb.append(',');
				sb.append(phone);
				if (!TextUtils.isEmpty(contactName)) {
					sb.append(',');
					sb.append(contactName);
				}
				sb.append('\n');
				sb.append("\u3000\u3000一旦同意WHO请求，对方将收到你的信息（姓名、手机号），是否同意？");

				Dialog alertDialog = new MyAlertDialog.Builder(getActivity())
						.setTitle("WHO")
						.setIcon(R.drawable.message_who)
						.setMessage(sb.toString())
						.setPositiveButton("同意",
								new MyAlertDialog.OnClickListener() {
									@Override
									public boolean onClick(
											DialogInterface dialog, int which) {
										WhateverApplication
												.getMainTaskManager()
												.startTask(
														new WHOTask(
																TASK_TAG,
																nd.pmsession,
																true,
																WhateverApplication
																		.getApplication()));

										return true;
									}
								})
						.setNegativeButton("拒绝",
								new MyAlertDialog.OnClickListener() {
									@Override
									public boolean onClick(
											DialogInterface dialog, int which) {
										WhateverApplication
												.getMainTaskManager()
												.startTask(
														new WHOTask(
																TASK_TAG,
																nd.pmsession,
																false,
																WhateverApplication
																		.getApplication()));

										return true;
									}
								}).create();
				alertDialog.show();
				break;
			}
			case NotificationData.TYPE_WHO_REPLY:
				if (!TextUtils.isEmpty(nd.ext)) {
					String phone, realName, userName;
					try {
						JSONTokener jsonParser = new JSONTokener(nd.ext);

						jsonParser.nextTo('{');
						if (!jsonParser.more()) {
							throw new JSONException(
									"Failed to read return value.");
						}
						JSONObject jsonObj = (JSONObject) jsonParser
								.nextValue();

						phone = jsonObj.getString("phone");
						realName = jsonObj.getString("real_name");
						userName = jsonObj.getString("username");
					} catch (JSONException e) {
						e.printStackTrace();
						break;
					}

					String contactName = ContactSyncTask
							.contactEnabled(getActivity()) ? Util
							.findContactNameByNumber(getActivity(), phone)
							: null;

					StringBuilder sb = new StringBuilder();
					sb.append(userName);
					sb.append(',');
					sb.append(realName);
					sb.append(',');
					sb.append(phone);
					if (!TextUtils.isEmpty(contactName)) {
						sb.append(',');
						sb.append(contactName);
					}

					Dialog alertDialog = new MyAlertDialog.Builder(
							getActivity()).setTitle("WHO")
							.setIcon(R.drawable.message_who)
							.setMessage(sb.toString())
							.setPositiveButton("我知道了", null).create();
					alertDialog.show();
				}
				break;
			}
		}
	}

}
