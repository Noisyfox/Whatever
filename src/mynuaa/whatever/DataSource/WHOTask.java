package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class WHOTask extends Task {
	public static final int WHO_SUCCESS = 0;
	public static final int WHO_FAIL_INNER_ERROR = 1;
	public static final int WHO_FAIL_ALREADY = 2;

	private final OnWHOListener mOnWHOListener;
	private final String mCid;
	private final String mWid;
	private final boolean mAgree;

	private int mResult = WHO_SUCCESS;

	public WHOTask(String tag, String cid, OnWHOListener l) {
		super(tag);

		mOnWHOListener = l;
		mCid = cid;
		mWid = null;
		mAgree = false;
	}

	public WHOTask(String tag, String wid, boolean agree, OnWHOListener l) {
		super(tag);

		mOnWHOListener = l;
		mCid = null;
		mWid = wid;
		mAgree = agree;
	}

	@Override
	public void doTask() {
		if (mCid != null) {
			doAsk();
		} else {
			doApply();
		}
	}

	private void doAsk() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("nid", mCid);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_WHO_ASK, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = WHO_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http whoask", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			switch (errorCode) {
			case 0:
				mResult = WHO_SUCCESS;
				break;
			case 1:
				mResult = WHO_FAIL_ALREADY;
				break;
			default:
				mResult = WHO_FAIL_INNER_ERROR;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = WHO_FAIL_INNER_ERROR;
		}
	}

	private void doApply() {

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", UserSession.getCurrentSession().mSession);
		params.put("wid", mWid);
		params.put("agree", mAgree ? "1" : "0");

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_WHO_REPLY, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = WHO_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http whoreply", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			switch (errorCode) {
			case 0:
				mResult = WHO_SUCCESS;
				break;
			case 1:
				mResult = WHO_FAIL_ALREADY;
				break;
			default:
				mResult = WHO_FAIL_INNER_ERROR;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = WHO_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnWHOListener != null) {
			if (mCid != null) {
				mOnWHOListener.onWHOAsked(mResult, mCid);
			} else {
				mOnWHOListener.onWHOReplied(mResult, mWid, mAgree);
			}
		}
	}

	public static interface OnWHOListener {
		public void onWHOAsked(int result, String cid);

		public void onWHOReplied(int result, String wid, boolean agree);
	}
}
