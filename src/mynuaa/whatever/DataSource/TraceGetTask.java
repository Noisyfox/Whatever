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

public class TraceGetTask extends Task {
	public static final int GET_SUCCESS = 0;
	public static final int GET_FAIL_INNER_ERROR = 1;

	public static final int FILTER_GOOD = 1;
	public static final int FILTER_BAD = 2;
	public static final int FILTER_COMMENT = 3;
	public static final int FILTER_SEND = 4;
	public static final int FILTER_REPORT = 5;
	public static final int FILTER_PM = 6;
	public static final int FILTER_WHO = 7;

	private final int mFilter;
	private final String mSelectId;
	private final OnTraceGetListener mOnTraceGetListener;

	private int mResult = GET_SUCCESS;
	private List<MessageData> mMessages;

	public TraceGetTask(String tag, int filter, String selectId,
			OnTraceGetListener onTraceGetListener) {
		super(tag);
		mFilter = filter;
		mSelectId = selectId;
		mOnTraceGetListener = onTraceGetListener;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		if (mSelectId != null) {
			params.put("select_id", mSelectId);
		}

		String url = NetworkHelper.STR_SERVER_URL_TRACE_GET;

		switch (mFilter) {
		case FILTER_BAD:
			url += "bad";
			break;
		case FILTER_GOOD:
			url += "good";
			break;
		case FILTER_COMMENT:
			url += "comment";
			break;
		case FILTER_SEND:
			url += "news";
			break;
		case FILTER_REPORT:
			url += "report";
			break;
		case FILTER_PM:
			url += "pm";
			break;
		case FILTER_WHO:
			url += "who";
			break;
		default:

		}

		String result = NetworkHelper.doHttpRequest(url, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = GET_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http trace", result);

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
				String select_id = messageObj.getString("select_id");
				// String manner = messageObj.getString("manner");

				int background_color_index_i = Integer
						.parseInt(background_color_index);
				int background_texture_index_i = Integer
						.parseInt(background_texture_index);
				int good_count_i = Integer.parseInt(good_count);
				int bad_count_i = Integer.parseInt(bad_count);
				int comment_count_i = Integer.parseInt(comment_count);
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
				md.select_id = select_id;
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

			// MessageData.saveMessageCache(messages, mGetFilter);
			mMessages = messages;
			mResult = GET_SUCCESS;

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = GET_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnTraceGetListener != null) {
			mOnTraceGetListener.onTraceGet(mResult, mFilter, mSelectId,
					mMessages);
		}
	}

	public static interface OnTraceGetListener {
		public void onTraceGet(int result, int filter, String selectId,
				List<MessageData> messages);
	}
}
