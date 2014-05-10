package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class MessageMannerTask extends Task {
	public static final int PUT_SUCCESS = 0;
	public static final int PUT_FAIL_INNER_ERROR = 1;
	public static final int PUT_FAIL_ALREADY = 2;

	private final String mMessageId;
	private final int mManner;
	private final OnMannerPutListener mOnMannerPutListener;

	private int mResult = PUT_SUCCESS;
	private int mGood, mBad;

	public MessageMannerTask(String tag, String messageId, int manner,
			OnMannerPutListener onMannerPutListener) {
		super(tag);

		mMessageId = messageId;
		mManner = manner;
		mOnMannerPutListener = onMannerPutListener;
	}

	@Override
	public void doTask() {
		String session = UserSession.getCurrentSession().mSession;
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", session);
		params.put("nid", mMessageId);
		params.put("manner", String.valueOf(mManner));

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_MANNER_PUT, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = PUT_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http manner", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				String good_count = jsonObj.getString("goods");
				String bad_count = jsonObj.getString("bads");
				mGood = Integer.parseInt(good_count);
				mBad = Integer.parseInt(bad_count);

				mResult = PUT_SUCCESS;
			} else if (errorCode == -27) {
				mResult = PUT_FAIL_ALREADY;
			} else {
				mResult = PUT_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = PUT_FAIL_INNER_ERROR;
		}

	}

	@Override
	public void callback() {
		if (mOnMannerPutListener != null) {
			mOnMannerPutListener.onMannerPut(mResult, mMessageId, mManner,
					mGood, mBad);
		}
	}

	public static interface OnMannerPutListener {
		public void onMannerPut(int result, String messageId, int manner,
				int good_count, int bad_count);
	}
}
