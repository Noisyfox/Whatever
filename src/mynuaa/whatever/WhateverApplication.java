package mynuaa.whatever;

import mynuaa.whatever.DataSource.DataCenter;
import mynuaa.whatever.DataSource.ReportTask;
import mynuaa.whatever.DataSource.ReportTask.OnReportListener;
import mynuaa.whatever.DataSource.TaskManager;
import android.app.Application;
import android.os.Looper;
import android.widget.Toast;

public class WhateverApplication extends Application implements
		OnReportListener {

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
}
