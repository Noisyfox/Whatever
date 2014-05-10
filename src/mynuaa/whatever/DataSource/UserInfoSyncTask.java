package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class UserInfoSyncTask extends Task {

	public static final int UPLOAD = 1;// 上传用户信息
	public static final int DOWNLOAD = 2;// 下载用户信息
	public static final int LOCAL = 3;// 读取本地用户信息

	public static final int SYNC_SUCCESS = 0;
	public static final int SYNC_FAIL_INNER_ERROR = 1;

	private final UserSession mSession;
	private final OnUserInfoSyncListener mOnUserInfoSyncListener;
	private final int mSyncType;

	private final String mRealName, mPhone;

	private int mResult;

	public UserInfoSyncTask(String tag, UserSession session, int syncType,
			OnUserInfoSyncListener onUserInfoSyncListener) {
		super(tag);
		mSession = session;
		mSyncType = syncType;
		mOnUserInfoSyncListener = onUserInfoSyncListener;

		mRealName = null;
		mPhone = null;
	}

	public UserInfoSyncTask(String tag, UserSession session, String realName,
			String phone, OnUserInfoSyncListener onUserInfoSyncListener) {
		super(tag);
		mSession = session;
		mSyncType = UPLOAD;
		mOnUserInfoSyncListener = onUserInfoSyncListener;
		mRealName = realName;
		mPhone = phone;
	}

	@Override
	public void doTask() {
		switch (mSyncType) {
		case UPLOAD: {
			upload();
			break;
		}
		case DOWNLOAD: {
			download();
			break;
		}
		case LOCAL: {
			local();
			break;
		}
		}
	}

	private void upload() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSession.mSession);
		params.put("real_name", mRealName);
		params.put("phone", mPhone);

		String result = NetworkHelper
				.doHttpRequest(NetworkHelper.STR_SERVER_URL_USERINFO_UPDATE,
						params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = SYNC_FAIL_INNER_ERROR;
			return;
		}
		Log.d("sync", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				mResult = SYNC_SUCCESS;
				mSession.mUserInfo.mPhone = mPhone;
				mSession.mUserInfo.mName = mRealName;
			} else {
				mResult = SYNC_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = SYNC_FAIL_INNER_ERROR;
		}

	}

	private void local() {
		mSession.mUserInfo.loadHeadBitmap(true);
		mResult = SYNC_SUCCESS;
	}

	private void download() {
		mSession.mUserInfo.loadHeadBitmap(false);
		mResult = SYNC_SUCCESS;
	}

	@Override
	public void callback() {
		if (mOnUserInfoSyncListener != null) {
			mOnUserInfoSyncListener
					.onSyncFinished(mResult, mSyncType, mSession);
		}
	}

	public static interface OnUserInfoSyncListener {
		public void onSyncFinished(int result, int syncType, UserSession session);
	}
}
