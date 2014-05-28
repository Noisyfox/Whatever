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
			Toast.makeText(this, "�ٱ��ɹ�", Toast.LENGTH_SHORT).show();
		} else if (result == ReportTask.REPORT_FAIL_ALREADY) {
			Toast.makeText(this, "���Ѿ��ٱ�������״̬��", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "�ٱ�ʧ�ܣ����Ժ�����", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onWHOAsked(int result, String cid) {
	}

	@Override
	public void onWHOReplied(int result, String wid, boolean agree) {
		switch (result) {
		case WHOTask.WHO_SUCCESS:
			Toast.makeText(this, agree ? "��ͬ����Ta������" : "��ܾ���Ta������",
					Toast.LENGTH_SHORT).show();
			break;
		case WHOTask.WHO_FAIL_ALREADY:
			Toast.makeText(this, "���Ѿ��ظ�������WHO����~", Toast.LENGTH_SHORT).show();
			break;
		case WHOTask.WHO_FAIL_INNER_ERROR:
			Toast.makeText(this, "ʧ����~���Ժ�����", Toast.LENGTH_SHORT).show();
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
