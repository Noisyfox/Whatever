package mynuaa.whatever;

import java.util.ArrayList;

import mynuaa.whatever.DataSource.DataCenter;
import mynuaa.whatever.DataSource.NotificationCheckTask;
import mynuaa.whatever.DataSource.NotificationCheckTask.OnNotificationCheckListener;
import mynuaa.whatever.DataSource.ReportTask;
import mynuaa.whatever.DataSource.WHOTask;
import mynuaa.whatever.DataSource.ReportTask.OnReportListener;
import mynuaa.whatever.DataSource.TaskManager;
import mynuaa.whatever.DataSource.WHOTask.OnWHOListener;
import android.app.Application;
import android.os.Looper;
import android.widget.Toast;

public class WhateverApplication extends Application implements
		OnReportListener, OnWHOListener, OnNotificationCheckListener {

	private static TaskManager mTaskManager;
	private static WhateverApplication mWhateverApplication;

	@Override
	public void onCreate() {
		super.onCreate();

		mWhateverApplication = this;

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

		DataCenter.initDataCenter(getApplicationContext());

		mTaskManager = new TaskManager(Looper.getMainLooper());
	}

	@Override
	public void onTerminate() {
		DataCenter.stopLocationService();
		DataCenter.closeAllDatabase();
		super.onTerminate();
	}

	public static TaskManager getMainTaskManager() {
		return mTaskManager;
	}

	public static WhateverApplication getApplication() {
		return WhateverApplication.mWhateverApplication;
	}

	@Override
	public void onReport(int result) {
		if (result == ReportTask.REPORT_SUCCESS) {
			Toast.makeText(this, "举报成功", Toast.LENGTH_SHORT).show();
		} else if (result == ReportTask.REPORT_FAIL_ALREADY) {
			Toast.makeText(this, "你已经举报过这条状态了", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "举报失败，请稍后再试", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onWHOAsked(int result, String cid) {
	}

	@Override
	public void onWHOReplied(int result, String wid, boolean agree) {
		switch (result) {
		case WHOTask.WHO_SUCCESS:
			Toast.makeText(this, agree ? "你同意了Ta的请求" : "你拒绝了Ta的请求",
					Toast.LENGTH_SHORT).show();
			break;
		case WHOTask.WHO_FAIL_ALREADY:
			Toast.makeText(this, "你已经回复过这条WHO了啦~", Toast.LENGTH_SHORT).show();
			break;
		case WHOTask.WHO_FAIL_INNER_ERROR:
			Toast.makeText(this, "失败啦~请稍后再试", Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private ArrayList<OnNotificationCheckListener> mOnNotificationCheckListeners = new ArrayList<OnNotificationCheckListener>();

	public void registerOnNotificationCheckListener(
			OnNotificationCheckListener l) {
		synchronized (mOnNotificationCheckListeners) {
			if (!mOnNotificationCheckListeners.contains(l)) {
				mOnNotificationCheckListeners.add(l);
			}
		}
	}

	public void unregisterOnNotificationCheckListener(
			OnNotificationCheckListener l) {
		synchronized (mOnNotificationCheckListeners) {
			mOnNotificationCheckListeners.remove(l);
		}
	}

	public void checkNoitifcation() {
		mTaskManager.startTask(new NotificationCheckTask("Global", this));
	}

	@Override
	public void onNotificationCheck(int result, int unreadCount) {
		synchronized (mOnNotificationCheckListeners) {
			for (OnNotificationCheckListener l : mOnNotificationCheckListeners) {
				l.onNotificationCheck(result, unreadCount);
			}
		}
	}
}
