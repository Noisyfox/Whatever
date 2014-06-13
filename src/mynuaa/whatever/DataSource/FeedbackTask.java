package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class FeedbackTask extends Task {
	public static final int SEND_SUCCESS = 0;
	public static final int SEND_FAIL_INNER_ERROR = 1;

	private final String mMessage;
	private final String mContact;
	private final OnFeedbackSendListener mOnFeedbackSendListener;

	private int mResult = SEND_SUCCESS;

	public FeedbackTask(String tag, String message, String contact,
			OnFeedbackSendListener l) {
		super(tag);
		mMessage = message;
		mContact = contact;
		mOnFeedbackSendListener = l;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("message", mMessage);
		params.put("contact", mContact);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_FEEDBACK, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = SEND_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http feedback", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				mResult = SEND_SUCCESS;
			} else {
				mResult = SEND_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = SEND_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnFeedbackSendListener != null) {
			mOnFeedbackSendListener.onFeedbackSend(mResult);
		}
	}

	public static interface OnFeedbackSendListener {
		public void onFeedbackSend(int result);
	}
}
