package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class LoginTask extends Task {

	public static final int LOGIN_SUCCESS = 0;
	public static final int LOGIN_FAIL_WRONG = 1;
	public static final int LOGIN_FAIL_SESSION_TIME_OUT = 2;
	public static final int LOGIN_FAIL_INNER_ERROR = 3;

	private final OnLoginListener mOnLoginListener;

	private String mUser;
	private String mPsw;

	private int mResult = LOGIN_SUCCESS;
	private UserSession mSession = null;

	/**
	 * 检查该用户会话是否有效
	 * 
	 * @param session
	 */
	public LoginTask(String tag, UserSession session,
			OnLoginListener onLoginListener) {
		super(tag);
		mSession = session;
		mOnLoginListener = onLoginListener;
	}

	/**
	 * 用户登陆
	 * 
	 * @param user
	 * @param psw
	 */
	public LoginTask(String tag, String user, String psw,
			OnLoginListener onLoginListener) {
		super(tag);
		mUser = user;
		mPsw = psw;
		mOnLoginListener = onLoginListener;
	}

	private void login() {

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("username", mUser);
		params.put("password", mPsw);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_LOGIN, params.entrySet());

		parse(result, null);
	}

	private void checkSession() {

		long ct = System.currentTimeMillis();

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSession.mSession);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_LOGIN, params.entrySet());

		parse(result, mSession);

		ct = System.currentTimeMillis() - ct;
		if (ct < 1200) {
			try {
				Thread.sleep(1200 - ct);
			} catch (InterruptedException e) {
			}
		}
	}

	private UserSession parse(String result, UserSession oSession) {

		if (TextUtils.isEmpty(result)) {
			mResult = LOGIN_FAIL_INNER_ERROR;
			return null;
		}

		Log.d("login", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == -1 || errorCode == -2) {
				mResult = LOGIN_FAIL_WRONG;
			} else if (errorCode == -3 || errorCode == -4) {
				mResult = LOGIN_FAIL_SESSION_TIME_OUT;
			} else if (errorCode == 0) {
				String session = jsonObj.getString("session");
				String userName = jsonObj.getString("username");
				String realName = jsonObj.getString("real_name");
				String phone = jsonObj.getString("phone");
				String head = jsonObj.getString("avator");

				if (oSession == null)
					oSession = new UserSession();

				oSession.mSession = session;
				oSession.mUserInfo.mUid = userName;
				oSession.mUserInfo.mName = realName;
				oSession.mUserInfo.mPhone = phone;
				oSession.mUserInfo.mHead = head;

				UserSession.setCurrentSession(oSession);

				mSession = oSession;
				mResult = LOGIN_SUCCESS;
			} else {
				mResult = LOGIN_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = LOGIN_FAIL_INNER_ERROR;
			return null;
		}

		return oSession;
	}

	@Override
	public void doTask() {
		if (mSession == null) {
			login();
		} else {
			checkSession();
		}
	}

	@Override
	public void callback() {
		if (mOnLoginListener != null) {
			mOnLoginListener.onLoginFinished(mResult, mSession);
		}
	}

	public static interface OnLoginListener {
		public void onLoginFinished(int result, UserSession session);
	}
}
