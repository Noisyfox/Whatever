package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import mynuaa.whatever.Util;
import mynuaa.whatever.DataSource.DataCenter.Location;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class MessagePostTask extends Task {

	private final MessageData mMessage;
	private final OnMessagePostListener mOnMessagePostListener;

	public static final int POST_SUCCESS = 0;
	public static final int POST_FAIL_INNER_ERROR = 1;
	public static final int POST_FAIL_IMAGE_UPLOAD_FAIL = 2;
	public static final int POST_FAIL_LOCAION_FAIL = 3;

	private int mResult = POST_SUCCESS;

	public MessagePostTask(String tag, MessageData message,
			OnMessagePostListener onMessagePostListener) {
		super(tag);

		mMessage = message;
		mOnMessagePostListener = onMessagePostListener;
	}

	@Override
	public void doTask() {
		String session = UserSession.getCurrentSession().mSession;
		String image_cid = "";

		// 定位
		Location l = DataCenter.requireLocationSync(5000);
		if (l == null) {
			mResult = POST_FAIL_LOCAION_FAIL;
			return;
		}

		// 上传图片
		if (mMessage.image != null) {
			String result = NetworkHelper.uploadImage(
					NetworkHelper.STR_SERVER_URL_IMAGE_UPLOAD, session,
					mMessage.image);

			if (TextUtils.isEmpty(result)) {
				mResult = POST_FAIL_IMAGE_UPLOAD_FAIL;
				return;
			}

			Log.d("image upload", result);

			try {
				JSONTokener jsonParser = new JSONTokener(result);

				jsonParser.nextTo('{');
				if (!jsonParser.more()) {
					throw new JSONException("Failed to read return value.");
				}
				JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

				int errorCode = jsonObj.getInt("ErrorCode");

				if (errorCode != 0) {
					mResult = POST_FAIL_IMAGE_UPLOAD_FAIL;
					String m = jsonObj.getString("ErrorMsg");
					Log.d("err", m);
					return;
				}

				image_cid = jsonObj.getString("image_id");

			} catch (JSONException e) {
				e.printStackTrace();
				mResult = POST_FAIL_IMAGE_UPLOAD_FAIL;
				return;
			}
		}

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", session);
		params.put("content", Util.messageEncode(mMessage.content));
		params.put("location_x", String.valueOf(l.getLongitude()));
		params.put("location_y", String.valueOf(l.getLatitude()));
		params.put("texture", String.valueOf(mMessage.background_texture_index));
		params.put("color", String.valueOf(mMessage.background_color_index));
		if (!image_cid.isEmpty()) {
			params.put("imageid", image_cid);
		}

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_MESSAGE_POST, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = POST_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http msgpost", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
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
		if (mOnMessagePostListener != null) {
			mOnMessagePostListener.onMessagePost(mResult, mMessage);
		}
	}

	public static interface OnMessagePostListener {
		public void onMessagePost(int result, MessageData message);
	}
}
