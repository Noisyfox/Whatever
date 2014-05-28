package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class PMRequireSessionTask extends Task {
	public static final int GET_SUCCESS = 0;
	public static final int GET_FAIL_INNER_ERROR = 1;
	public static final int GET_FAIL_SELF = 2;

	private final String mNcid;
	private final OnSessionGetListener mOnSessionGetListener;

	private int mResult = GET_SUCCESS;
	private String mSession = null;

	public PMRequireSessionTask(String tag, String ncid,
			OnSessionGetListener onSessionGetListener) {
		super(tag);

		mNcid = ncid;
		mOnSessionGetListener = onSessionGetListener;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("nid", mNcid);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_PM_REQUIRESESSION,
				params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = GET_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http pm session", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				String session = jsonObj.getString("Pm_sid");
				mSession = session;
				mResult = GET_SUCCESS;
			} else if (errorCode == 22) {
				mResult = GET_FAIL_SELF;
			} else {
				mResult = GET_FAIL_INNER_ERROR;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = GET_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnSessionGetListener != null) {
			mOnSessionGetListener.onSessionGet(mResult, mNcid, mSession);
		}
	}

	public static interface OnSessionGetListener {
		public void onSessionGet(int result, String ncid, String session);
	}

}
