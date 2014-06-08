package mynuaa.whatever;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import mynuaa.whatever.DataSource.ContactSyncTask;
import mynuaa.whatever.DataSource.ImageLoadTask;
import mynuaa.whatever.DataSource.NotificationData;
import mynuaa.whatever.DataSource.NotificationGetTask;
import mynuaa.whatever.DataSource.WHOTask;
import mynuaa.whatever.DataSource.ImageLoadTask.OnImageLoadListener;
import mynuaa.whatever.DataSource.NotificationGetTask.OnNotificationGetListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NotifyListActivity extends SherlockActivity implements
		OnClickListener, OnItemClickListener, OnRefreshListener<ListView>,
		OnNotificationGetListener {
	private static final String TASK_TAG = "task_notify_list_activity";

	private int mNotifyType;

	private Button btn_markAllRead;
	private View loadingView;// 加载视图的布局
	private PullToRefreshListView rootListView;
	private ListView listView;
	private boolean loading = false;
	private NotifyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notify_list);

		mNotifyType = getIntent().getIntExtra("notifyType", -1);
		int titleRes = 0;
		switch (mNotifyType) {
		case NotificationData.TYPE_GOOD:
			titleRes = R.string.btn_message_good;
			break;
		case NotificationData.TYPE_BAD:
			titleRes = R.string.btn_message_bad;
			break;
		case NotificationData.TYPE_COMMENT:
			titleRes = R.string.btn_message_comment;
			break;
		case NotificationData.TYPE_REPORT:
			titleRes = R.string.btn_message_report;
			break;
		case NotificationData.TYPE_PM:
			titleRes = R.string.btn_message_pm;
			break;
		case NotificationData.TYPE_WHO:
			titleRes = R.string.btn_message_who;
			break;
		default:
			Util.setupCommonActionBar(this, R.string.drawer_title_message);
			return;
		}

		Util.setupCommonActionBar(this, titleRes);
		btn_markAllRead = (Button) getSupportActionBar().getCustomView()
				.findViewById(R.id.right_btn);
		btn_markAllRead.setVisibility(View.VISIBLE);
		btn_markAllRead.setText(R.string.mark_all_read);
		btn_markAllRead.setOnClickListener(this);

		loadingView = LayoutInflater.from(this).inflate(R.layout.footer, null);
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_read);

		rootListView = (PullToRefreshListView) findViewById(R.id.main_list);
		listView = rootListView.getRefreshableView();
		listView.addFooterView(loadingView);

		switch (mNotifyType) {
		case NotificationData.TYPE_GOOD:
		case NotificationData.TYPE_BAD:
			mAdapter = new GoodBadAdapter(this, mNotifyType);
			break;
		case NotificationData.TYPE_COMMENT:
			mAdapter = new CommentAdapter(this);
			break;
		case NotificationData.TYPE_REPORT:
			mAdapter = new ReportAdapter(this);
			break;
		case NotificationData.TYPE_PM:
			mAdapter = new PMAdapter(this);
			break;
		case NotificationData.TYPE_WHO:
			mAdapter = new WHOAdapter(this);
			break;
		}

		rootListView.setAdapter(mAdapter);
		rootListView.setOnRefreshListener(this);
		listView.setOnItemClickListener(this);
		mAdapter.mergeData(true,
				NotificationData.getUnreadNotification(mNotifyType));

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
	}

	@Override
	public void finish() {
		setResult(1);
		super.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
	}

	@Override
	protected void onDestroy() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		mAdapter.markAllRead();
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
			mAdapter.notifyDataSetChanged();

			mAdapter.onItemClick(this, nd, parent, view, position, id);
		}
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		updateCurrendData(true);
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
					new NotificationGetTask(TASK_TAG, mNotifyType, this));
		} else {
			WhateverApplication.getMainTaskManager().startTask(
					new NotificationGetTask(TASK_TAG, mNotifyType, mAdapter
							.getLastId(), this));
		}
	}

	private synchronized void clearLoading() {
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_read);

		rootListView.post(new Runnable() {
			@Override
			public void run() {
				rootListView.onRefreshComplete();
				loading = false;
			}
		});
	}

	@Override
	public void onNotificationGet(int result, int readType, int notifyType,
			List<NotificationData> nds) {
		if (result == NotificationGetTask.GET_SUCCESS
				&& notifyType == mNotifyType) {
			if (readType == NotificationGetTask.TYPE_UNREAD) {
				mAdapter.mergeData(true, nds);
				clearLoading();
			} else if (readType == NotificationGetTask.TYPE_READ) {
				mAdapter.mergeData(false, nds);
				clearLoading();
			}
		}
	}

	// ----------------------------------------------------------------------------------

	private abstract static class NotifyAdapter extends BaseAdapter {

		protected LinkedList<NotificationData> mData = new LinkedList<NotificationData>();

		protected final Context mContext;

		public NotifyAdapter(Context context) {
			mContext = context;
		}

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
		}

		public synchronized String getLastId() {
			return mData.size() == 0 ? null : mData.getLast().cid;
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
			return position;
		}

		public abstract void onItemClick(Activity activity,
				NotificationData nd, AdapterView<?> parent, View view,
				int position, long id);
	}

	private abstract static class CommonNotifyAdapter extends NotifyAdapter {

		class ViewHolder {
			TextView tv_content, tv_message;
			TriangleCornerView tcv_corner;
		}

		public CommonNotifyAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.notify_list_item_common, parent, false);
				holder = new ViewHolder();
				holder.tv_content = (TextView) convertView
						.findViewById(R.id.textView_content);
				holder.tv_message = (TextView) convertView
						.findViewById(R.id.textView_message);
				holder.tcv_corner = (TriangleCornerView) convertView
						.findViewById(R.id.imageView_corner);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			NotificationData nd = mData.get(position);

			holder.tcv_corner.setVisibility(nd.isRead ? View.INVISIBLE
					: View.VISIBLE);
			holder.tcv_corner.setColor(MessageTheme.getColor(nd.theme_color,
					MessageTheme.COLOR_CORNER));
			holder.tv_content.setText("“" + nd.content + "”");
			setView(nd, holder);

			return convertView;
		}

		public abstract void setView(NotificationData nd, ViewHolder holder);
	}

	private static class GoodBadAdapter extends NotifyAdapter {

		class ViewHolder {
			TextView tv_content, tv_count;
			TriangleCornerView tcv_corner;
		}

		private final int mResid;

		public GoodBadAdapter(Context context, int type) {
			super(context);

			mResid = type == NotificationData.TYPE_GOOD ? R.string.notify_type_good
					: R.string.notify_type_bad;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.notify_list_item_goodbad, parent, false);
				holder = new ViewHolder();
				holder.tv_content = (TextView) convertView
						.findViewById(R.id.textView_content);
				holder.tv_count = (TextView) convertView
						.findViewById(R.id.textView_count);
				holder.tcv_corner = (TriangleCornerView) convertView
						.findViewById(R.id.imageView_corner);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			NotificationData nd = mData.get(position);

			holder.tcv_corner.setVisibility(nd.isRead ? View.INVISIBLE
					: View.VISIBLE);
			holder.tcv_corner.setColor(MessageTheme.getColor(nd.theme_color,
					MessageTheme.COLOR_CORNER));
			holder.tv_content.setText("“" + nd.content + "”");
			holder.tv_count.setText(mContext.getResources().getString(mResid,
					nd.count));

			return convertView;
		}

		@Override
		public void onItemClick(Activity activity, NotificationData nd,
				AdapterView<?> parent, View view, int position, long id) {
			MessageActivity.showMessage(activity, nd.ncid);
		}
	}

	private static class ReportAdapter extends CommonNotifyAdapter {

		public ReportAdapter(Context context) {
			super(context);
		}

		@Override
		public void setView(NotificationData nd, ViewHolder holder) {
			holder.tv_message.setText(R.string.notify_type_report);
		}

		@Override
		public void onItemClick(Activity activity, NotificationData nd,
				AdapterView<?> parent, View view, int position, long id) {
		}

	}

	private static class PMAdapter extends CommonNotifyAdapter {

		public PMAdapter(Context context) {
			super(context);
		}

		@Override
		public void setView(NotificationData nd, ViewHolder holder) {
			holder.tv_message.setText(Html.fromHtml(mContext.getString(
					R.string.notify_type_pm, "二狗蛋", nd.note)));
		}

		@Override
		public void onItemClick(Activity activity, NotificationData nd,
				AdapterView<?> parent, View view, int position, long id) {
			PMActivity.startPMSession(activity, null, nd.pmsession);
		}
	}

	private static class CommentAdapter extends CommonNotifyAdapter {

		public CommentAdapter(Context context) {
			super(context);
		}

		@Override
		public void setView(NotificationData nd, ViewHolder holder) {
			holder.tv_message.setText("\"" + nd.note + "\"");
		}

		@Override
		public void onItemClick(Activity activity, NotificationData nd,
				AdapterView<?> parent, View view, int position, long id) {
			CommentActivity.showComment(activity, nd.ncid, true);
		}

	}

	private static class DialogWrapper implements OnImageLoadListener {
		WeakReference<Dialog> wr_dialog;
		WeakReference<ImageView> wr_image;

		public DialogWrapper(Dialog fatherDialog, ImageView headView) {
			wr_dialog = new WeakReference<Dialog>(fatherDialog);
			wr_image = new WeakReference<ImageView>(headView);
		}

		@Override
		public void onImageLoaded(int taskCode, String cid, String size,
				Bitmap image) {
			if (image == null) {
				return;
			}

			Dialog d = wr_dialog.get();
			if (d == null) {
				return;
			}

			if (!d.isShowing()) {
				return;
			}

			ImageView iv = wr_image.get();
			if (iv == null) {
				return;
			}

			iv.setImageBitmap(image);
		}

	}

	private static class WHOAdapter extends CommonNotifyAdapter {

		public WHOAdapter(Context context) {
			super(context);
		}

		@Override
		public void setView(NotificationData nd, ViewHolder holder) {
			if (nd.type == NotificationData.TYPE_WHO) {
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
					return;
				}

				holder.tv_message.setText(Html.fromHtml(mContext.getString(
						R.string.notify_type_who, userName)));
			} else if (nd.type == NotificationData.TYPE_WHO_REPLY) {
				holder.tv_message
						.setText(TextUtils.isEmpty(nd.ext) ? R.string.notify_type_who_reply_disagree
								: R.string.notify_type_who_reply_agree);
			}
		}

		@Override
		public void onItemClick(Activity activity, final NotificationData nd,
				AdapterView<?> parent, View view, int position, long id) {
			if (nd.type == NotificationData.TYPE_WHO) {
				String phone, realName, userName, uid;
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
					uid = jsonObj.getString("uid");
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				String contactName = ContactSyncTask.contactEnabled(activity) ? Util
						.findContactNameByNumber(activity, phone) : null;

				final View askView = LayoutInflater.from(activity).inflate(
						R.layout.dialog_who, null);
				final ImageView iv_head = (ImageView) askView
						.findViewById(R.id.imageView_head);

				TextView tv_id = (TextView) askView
						.findViewById(R.id.textView_id), tv_name = (TextView) askView
						.findViewById(R.id.textView_name), tv_phone = (TextView) askView
						.findViewById(R.id.textView_phone), tv_contact = (TextView) askView
						.findViewById(R.id.textView_contact);
				tv_id.setText(userName);
				tv_name.setText(realName);
				tv_phone.setText(phone);
				if (TextUtils.isEmpty(contactName)) {
					tv_contact.setText(R.string.who_contact_no);
				} else {
					tv_contact.setText(activity.getString(R.string.who_contact,
							contactName));
				}
				Dialog alertDialog = new MyAlertDialog.Builder(activity)
						.setTitle("WHO")
						.setIcon(R.drawable.message_who)
						.setView(askView)
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
				DialogWrapper dw = new DialogWrapper(alertDialog, iv_head);
				WhateverApplication.getMainTaskManager().startTask(
						new ImageLoadTask(TASK_TAG, 0, "avatar_" + uid, "big",
								dw));
			} else if (nd.type == NotificationData.TYPE_WHO_REPLY) {
				if (!TextUtils.isEmpty(nd.ext)) {
					String phone, realName, userName, uid;
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
						uid = jsonObj.getString("uid");
					} catch (JSONException e) {
						e.printStackTrace();
						return;
					}

					String contactName = ContactSyncTask
							.contactEnabled(activity) ? Util
							.findContactNameByNumber(activity, phone) : null;

					final View askView = LayoutInflater.from(activity).inflate(
							R.layout.dialog_who, null);
					final ImageView iv_head = (ImageView) askView
							.findViewById(R.id.imageView_head);
					TextView tv_id = (TextView) askView
							.findViewById(R.id.textView_id), tv_name = (TextView) askView
							.findViewById(R.id.textView_name), tv_phone = (TextView) askView
							.findViewById(R.id.textView_phone), tv_contact = (TextView) askView
							.findViewById(R.id.textView_contact);
					askView.findViewById(R.id.textView_allow).setVisibility(
							View.GONE);
					tv_id.setText(userName);
					tv_name.setText(realName);
					tv_phone.setText(phone);
					if (TextUtils.isEmpty(contactName)) {
						tv_contact.setText(R.string.who_contact_no);
					} else {
						tv_contact.setText(activity.getString(
								R.string.who_contact, contactName));
					}

					Dialog alertDialog = new MyAlertDialog.Builder(activity)
							.setTitle("WHO").setIcon(R.drawable.message_who)
							.setView(askView).setPositiveButton("我知道了", null)
							.create();
					alertDialog.show();
					DialogWrapper dw = new DialogWrapper(alertDialog, iv_head);
					WhateverApplication.getMainTaskManager().startTask(
							new ImageLoadTask(TASK_TAG, 0, "avatar_" + uid,
									"big", dw));
				}
			}
		}

	}
}
