package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class ReportTask extends Task {

	public static final int REPORT_SUCCESS = 0;
	public static final int REPORT_FAIL_INNER_ERROR = 1;
	public static final int REPORT_FAIL_ALREADY = 2;

	private final OnReportListener mOnReportListener;
	private final String mCid;
	private final String mContent;

	private int mResult = REPORT_SUCCESS;

	public ReportTask(String tag, String cid, String content, OnReportListener l) {
		super(tag);

		mCid = cid;
		mContent = content;
		mOnReportListener = l;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("content", mContent);
		params.put("nid", mCid);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_REPORT, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = REPORT_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http report", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				mResult = REPORT_SUCCESS;
			} else if (errorCode == -5) {
				mResult = REPORT_FAIL_ALREADY;
			} else {
				mResult = REPORT_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = REPORT_FAIL_INNER_ERROR;
		}

	}

	@Override
	public void callback() {
		if (mOnReportListener != null) {
			mOnReportListener.onReport(mResult);
		}
	}

	public static interface OnReportListener {
		public void onReport(int result);
	}
}
