package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import mynuaa.whatever.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class PMPostTask extends Task {
	public static final int POST_SUCCESS = 0;
	public static final int POST_FAIL_INNER_ERROR = 1;

	private final PMData mPMData;
	private final OnPMPostListener mOnPMSendListener;

	private int mResult = POST_SUCCESS;

	public PMPostTask(String tag, PMData pmData,
			OnPMPostListener onPMPostListener) {
		super(tag);

		mPMData = pmData;
		mOnPMSendListener = onPMPostListener;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("Pm_sid", mPMData.session);
		params.put("content", Util.messageEncode(mPMData.content));

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_PM_POST, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = POST_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http pm post", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				String cid = jsonObj.getString("Pid");
				String time = jsonObj.getString("time");
				int cidI = Integer.parseInt(cid);
				mPMData.cid = cidI;
				mPMData.time = time;
				mPMData.timeL = Util.parseServerTime(time);
				mResult = POST_SUCCESS;
			} else {
				mResult = POST_FAIL_INNER_ERROR;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = POST_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnPMSendListener != null) {
			mOnPMSendListener.onPMPost(mResult, mPMData);
		}
	}

	public static interface OnPMPostListener {
		public void onPMPost(int result, PMData pmData);
	}

}
