package mynuaa.whatever.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mynuaa.whatever.Util;
import mynuaa.whatever.DataSource.DataCenter.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class MessageGetTask extends Task {

	public static final int GET_SUCCESS = 0;
	public static final int GET_FAIL_INNER_ERROR = 1;
	public static final int GET_FAIL_LOCAION_FAIL = 2;

	private final OnMessageGetListener mOnMessageGetListener;
	private final String mPrevId;
	private final double mLocX, mLocY;
	private final boolean mLocSet;

	private int mResult = GET_SUCCESS;

	List<MessageData> mMessages = null;

	private final int mGetFilter;

	public MessageGetTask(String tag, String prevMessageId, double location_x,
			double location_y, OnMessageGetListener onMessageGetListener) {
		super(tag);

		mOnMessageGetListener = onMessageGetListener;
		mPrevId = prevMessageId;
		mLocX = location_x;
		mLocY = location_y;
		mLocSet = true;

		mGetFilter = MessageData.MESSAGE_FILTER_LOCATION;
	}

	public MessageGetTask(String tag, String prevMessageId, int messageFilter,
			OnMessageGetListener onMessageGetListener) {
		super(tag);

		mOnMessageGetListener = onMessageGetListener;
		mPrevId = prevMessageId;
		mLocX = 0;
		mLocY = 0;
		mLocSet = false;

		mGetFilter = messageFilter;
	}

	@Override
	public void doTask() {
		switch (mGetFilter) {
		case MessageData.MESSAGE_FILTER_LOCATION:
			getLoc();
			break;
		case MessageData.MESSAGE_FILTER_CONTACT:
			getContact();
			break;
		}
	}

	private void getLoc() {
		double longitude = mLocX, latitude = mLocY;

		if (!mLocSet) {
			// ¶¨Î»
			Location l = DataCenter.requireLocationSync(5000);
			if (l == null) {
				mResult = GET_FAIL_LOCAION_FAIL;
				return;
			}
			longitude = l.getLongitude();
			latitude = l.getLatitude();
		}

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("location_x", String.valueOf(longitude));
		params.put("location_y", String.valueOf(latitude));
		if (mPrevId != null) {
			params.put("last_nid", mPrevId);
		}

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_MESSAGE_GET, params.entrySet());

		parse(result);
	}

	private void getContact() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("contact", "1");
		if (mPrevId != null) {
			params.put("last_nid", mPrevId);
		}

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_MESSAGE_GET, params.entrySet());

		parse(result);
	}

	private void parse(String result) {

		if (TextUtils.isEmpty(result)) {
			mResult = GET_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http msgget", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode != 0) {
				mResult = GET_FAIL_INNER_ERROR;
				return;
			}

			JSONArray ja = jsonObj.getJSONArray("NewsList");

			List<MessageData> messages = new ArrayList<MessageData>();

			int size = ja.length();
			for (int i = 0; i < size; i++) {
				JSONObject messageObj = ja.getJSONObject(i);

				String cid = messageObj.getString("nid");
				String content = messageObj.getString("content");
				String image_cid = messageObj.getString("imageid");
				String time = messageObj.getString("time");

				String background_color_index = messageObj.getString("color");
				String background_texture_index = messageObj
						.getString("texture");
				String good_count = messageObj.getString("goods");
				String bad_count = messageObj.getString("bads");
				String comment_count = messageObj.getString("replys");
				String is_me = messageObj.getString("isme");
				// String manner = messageObj.getString("manner");

				int background_color_index_i = Integer
						.parseInt(background_color_index);
				int background_texture_index_i = Integer
						.parseInt(background_texture_index);
				int good_count_i = Integer.parseInt(good_count);
				int bad_count_i = Integer.parseInt(bad_count);
				int comment_count_i = Integer.parseInt(comment_count);
				boolean is_me_b = "1".equals(is_me);
				// int manner_i = Integer.parseInt(manner);

				if ("0".equals(image_cid)) {
					image_cid = "";
				}

				time = Util.formatTime(time);

				MessageData md = new MessageData();
				md.cid = cid;
				md.content = Util.messageDecode(content);
				md.image_cid = image_cid;
				md.time = time;
				md.background_color_index = background_color_index_i;
				md.background_texture_index = background_texture_index_i;
				md.good_count = good_count_i;
				md.bad_count = bad_count_i;
				md.comment_count = comment_count_i;
				md.is_me = is_me_b;
				// switch (manner_i) {
				// case 1:
				// md.put_bad = true;
				// break;
				// case 2:
				// md.put_good = true;
				// break;
				// }

				messages.add(md);
			}

			MessageData.saveMessageCache(messages, mGetFilter);
			mMessages = messages;

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = GET_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnMessageGetListener != null) {
			mOnMessageGetListener.onMessageGet(mResult, mGetFilter, mPrevId,
					mMessages);
		}
	}

	public static interface OnMessageGetListener {
		public void onMessageGet(int result, int filter, String prevId,
				List<MessageData> messages);
	}
}
