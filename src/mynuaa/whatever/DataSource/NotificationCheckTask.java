package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class NotificationCheckTask extends Task {
	public static final int CHECK_SUCCESS = 0;
	public static final int CHECK_FAIL_INNER_ERROR = 1;

	private final OnNotificationCheckListener mOnNotificationCheckListener;

	private int mResult = CHECK_SUCCESS;
	private int mUnreadCount;

	public NotificationCheckTask(String tag, OnNotificationCheckListener l) {
		super(tag);

		mOnNotificationCheckListener = l;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_NOTIFY_CHECK, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = CHECK_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http notichk", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode != 0) {
				mResult = CHECK_FAIL_INNER_ERROR;
				return;
			}

			String noti = jsonObj.getString("Notifications");

			mUnreadCount = Integer.parseInt(noti);

			mResult = CHECK_SUCCESS;
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = CHECK_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnNotificationCheckListener != null) {
			mOnNotificationCheckListener.onNotificationCheck(mResult,
					mUnreadCount);
		}
	}

	public static interface OnNotificationCheckListener {
		public void onNotificationCheck(int result, int unreadCount);
	}

	public static boolean checkEnabled(Context context) {
		SharedPreferences sp = context.getSharedPreferences("Settings",
				Context.MODE_PRIVATE);
		return sp.getBoolean("messageNoti", true);
	}
}
