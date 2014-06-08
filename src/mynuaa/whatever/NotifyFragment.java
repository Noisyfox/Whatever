package mynuaa.whatever;

import java.util.List;

import mynuaa.whatever.DataSource.NotificationData;
import mynuaa.whatever.DataSource.NotificationGetTask;
import mynuaa.whatever.DataSource.UserSession;
import mynuaa.whatever.DataSource.NotificationGetTask.OnNotificationGetListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class NotifyFragment extends SherlockFragment implements
		OnClickListener, OnNotificationGetListener {
	private static final String TASK_TAG = "task_notify_fragment";

	private static final int V_DEF[][] = {
			{ R.id.icon_good, R.drawable.message_good_light,
					R.string.btn_message_good },
			{ R.id.icon_bad, R.drawable.message_bad_light,
					R.string.btn_message_bad },
			{ R.id.icon_comment, R.drawable.message_comment_light,
					R.string.btn_message_comment },
			{ R.id.icon_report, R.drawable.message_report_light,
					R.string.btn_message_report },
			{ R.id.icon_pm, R.drawable.message_pm_light,
					R.string.btn_message_pm },
			{ R.id.icon_who, R.drawable.message_who_light,
					R.string.btn_message_who } };

	private static final int V_GOODS = 0;
	private static final int V_BADS = 1;
	private static final int V_COMMENT = 2;
	private static final int V_REPORT = 3;
	private static final int V_PM = 4;
	private static final int V_WHO = 5;

	private static class IconViewHolder {
		ImageView iv_background;
		ImageView iv_icon;
		TextView tv_title;
		TextView tv_count;
		View btn;
	}

	private final IconViewHolder mIcons[] = { new IconViewHolder(),
			new IconViewHolder(), new IconViewHolder(), new IconViewHolder(),
			new IconViewHolder(), new IconViewHolder() };

	private TextView mTextView_noti;
	private View loadingView;
	private boolean loading = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_notify, container,
				false);

		loadingView = rootView.findViewById(R.id.pm_load_process);
		for (int i = 0; i < V_DEF.length; i++) {
			setIconHolder(rootView, V_DEF[i][0], mIcons[i]);
			IconViewHolder holder = mIcons[i];
			setUnreadCount(holder, 100);
			holder.iv_icon.setImageResource(V_DEF[i][1]);
			holder.tv_title.setText(V_DEF[i][2]);
			holder.btn.setOnClickListener(this);
			holder.btn.setTag(i);
		}
		mTextView_noti = (TextView) rootView.findViewById(R.id.textView_noti);

		setUnreadCount(0);

		refreshUnreadCount();

		setHasOptionsMenu(true);

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		WhateverApplication.getMainTaskManager().startTask(
				new NotificationGetTask(TASK_TAG, -1, this));

		return rootView;
	}

	@Override
	public void onNotificationGet(int result, int readType, int notifyType,
			List<NotificationData> nds) {
		loading = false;
		loadingView.setVisibility(View.GONE);
		refreshUnreadCount();

		if (result != NotificationGetTask.GET_SUCCESS) {
			Toast.makeText(getActivity(), "Ë¢ÐÂÊ§°ÜÀ²£¡", Toast.LENGTH_SHORT).show();
		} else {
			UserSession session = UserSession.getCurrentSession();
			session.clearUnreadNotification();
			session.saveAsLocalSession(getActivity());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.notipage, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		FragmentActivity a = this.getActivity();

		if (MainActivity.class.isInstance(a)) {
			MainActivity ma = (MainActivity) a;
			boolean drawerOpen = ma.mDrawerLayout.isDrawerOpen(ma.mDrawerList);

			menu.findItem(R.id.mark_all_read).setVisible(!drawerOpen);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!loading && item.getItemId() == R.id.mark_all_read) {
			List<NotificationData> nds = NotificationData
					.getUnreadNotification();
			for (NotificationData nd : nds) {
				NotificationData.setRead(nd.cid);
			}
			refreshUnreadCount();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyView() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		Object o = v.getTag();
		if (!(o instanceof Integer)) {
			return;
		}
		int t = (Integer) o;
		Intent i = new Intent(getActivity(), NotifyListActivity.class);

		switch (t) {
		case V_GOODS:
			i.putExtra("notifyType", NotificationData.TYPE_GOOD);
			break;
		case V_BADS:
			i.putExtra("notifyType", NotificationData.TYPE_BAD);
			break;
		case V_COMMENT:
			i.putExtra("notifyType", NotificationData.TYPE_COMMENT);
			break;
		case V_REPORT:
			i.putExtra("notifyType", NotificationData.TYPE_REPORT);
			break;
		case V_PM:
			i.putExtra("notifyType", NotificationData.TYPE_PM);
			break;
		case V_WHO:
			i.putExtra("notifyType", NotificationData.TYPE_WHO);
			break;
		default:
			return;
		}
		Activity activity = getActivity();

		startActivityForResult(i, 1);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		refreshUnreadCount();
	}

	private void setIconHolder(View rootView, int iconId, IconViewHolder holder) {
		View v = rootView.findViewById(iconId);
		holder.iv_background = (ImageView) v
				.findViewById(R.id.imageView_background);
		holder.iv_icon = (ImageView) v.findViewById(R.id.imageView_icon);
		holder.tv_title = (TextView) v.findViewById(R.id.textView_title);
		holder.tv_count = (TextView) v.findViewById(R.id.textView_count);
		holder.btn = v.findViewById(R.id.button_notify);
	}

	private void setUnreadCount(int count) {
		if (count <= 0) {
			mTextView_noti.setText(R.string.notify_no_unread);
			((MainActivity) getActivity()).clearNotification();
		} else {
			String str = getActivity().getResources().getString(
					R.string.notify_has_unread,
					count > 99 ? "" : String.valueOf(count));
			mTextView_noti.setText(str);
		}
	}

	private void setUnreadCount(IconViewHolder holder, int count) {
		if (count <= 0) {
			holder.iv_background
					.setImageResource(R.drawable.notify_item_normal);
			holder.tv_count.setVisibility(View.GONE);
		} else {
			holder.iv_background
					.setImageResource(R.drawable.notify_item_unread);
			holder.tv_count.setVisibility(View.VISIBLE);
			holder.tv_count
					.setText(count > 99 ? "³¬¹ý99" : String.valueOf(count));
		}
	}

	private void refreshUnreadCount() {
		List<NotificationData> ns = NotificationData.getUnreadNotification();

		int c[] = new int[6];
		for (NotificationData n : ns) {
			switch (n.type) {
			case NotificationData.TYPE_GOOD:
				c[V_GOODS]++;
				break;
			case NotificationData.TYPE_BAD:
				c[V_BADS]++;
				break;
			case NotificationData.TYPE_COMMENT:
				c[V_COMMENT]++;
				break;
			case NotificationData.TYPE_PM:
				c[V_PM]++;
				break;
			case NotificationData.TYPE_REPORT:
				c[V_REPORT]++;
				break;
			case NotificationData.TYPE_WHO:
			case NotificationData.TYPE_WHO_REPLY:
				c[V_WHO]++;
				break;
			}
		}

		setUnreadCount(ns.size());
		for (int i = 0; i < c.length; i++) {
			setUnreadCount(mIcons[i], c[i]);
		}
	}
}
