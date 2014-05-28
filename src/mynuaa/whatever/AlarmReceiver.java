package mynuaa.whatever;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	public static final String ALARM_ACTION_CHECK_NOTIFICATION = "mynuaa.whatever.checkNotification";
	public static final int SENDER_CODE_CHECK_NOTIFICATION = 0;
	public static final long INTERVAL_REFRESHPSW = 1 * 60 * 1000;// 1∑÷÷”ºÏ≤‚“ª¥Œ

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ALARM_ACTION_CHECK_NOTIFICATION.equals(intent.getAction())) {
			WhateverApplication.getApplication().checkNoitifcation();
		}
	}

	public static void startAlarm(Context context) {
		Intent intent = new Intent(ALARM_ACTION_CHECK_NOTIFICATION);
		PendingIntent pi = PendingIntent.getBroadcast(context,
				SENDER_CODE_CHECK_NOTIFICATION, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				INTERVAL_REFRESHPSW, pi);
	}

	public static void stopAlarm(Context context) {
		Intent intent = new Intent(ALARM_ACTION_CHECK_NOTIFICATION);
		PendingIntent pi = PendingIntent.getBroadcast(context,
				SENDER_CODE_CHECK_NOTIFICATION, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
	}
}
