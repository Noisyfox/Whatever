package mynuaa.whatever.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mynuaa.whatever.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class PMGetTask extends Task {
	public static final int GET_SUCCESS = 0;
	public static final int GET_FAIL_INNER_ERROR = 1;

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_UNREAD = 1;

	private final OnPMGetListener mOnPMGetListener;
	private final String mSession;
	private final int mType;

	private int mResult = GET_SUCCESS;
	private List<PMData> mPMData = new ArrayList<PMData>();

	// 获取未读消息
	public PMGetTask(String tag, String session, OnPMGetListener onPMGetListener) {
		this(tag, session, TYPE_UNREAD, onPMGetListener);
	}

	public PMGetTask(String tag, String session, int type,
			OnPMGetListener onPMGetListener) {
		super(tag);

		mSession = session;
		mOnPMGetListener = onPMGetListener;
		mType = type;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", UserSession.getCurrentSession().mSession);
		params.put("Pm_sid", mSession);
		switch (mType) {
		case TYPE_NORMAL:
			params.put("isread", "2");
			break;
		case TYPE_UNREAD:
			params.put("isread", "0");
			break;
		}

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_PM_GET, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = GET_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http pm get", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				JSONArray ja = jsonObj.getJSONArray("PmList");
				int size = ja.length();
				for (int i = 0; i < size; i++) {
					JSONObject pmObj = ja.getJSONObject(i);

					String ncid = pmObj.getString("nid");
					String cid = pmObj.getString("pid");
					String content = pmObj.getString("content");
					String time = pmObj.getString("time");
					String session = pmObj.getString("pm_sid");
					String fromStr = pmObj.getString("from");

					int from = Integer.parseInt(fromStr);
					int cidI = Integer.parseInt(cid);

					PMData pd = new PMData();
					pd.ncid = ncid;
					pd.cid = cidI;
					pd.content = Util.messageDecode(content);
					pd.time = time;
					pd.session = session;
					pd.from = from;
					pd.timeL = Util.parseServerTime(time);

					pd.status = PMData.STATUS_NORMAL;

					mPMData.add(pd);
				}
				PMData.savePMCache(mPMData);

				mResult = GET_SUCCESS;
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
		if (mOnPMGetListener != null) {
			mOnPMGetListener.onPMGet(mResult, mType, mPMData);
		}
	}

	public static interface OnPMGetListener {
		public void onPMGet(int result, int type, List<PMData> pmData);
	}

}
