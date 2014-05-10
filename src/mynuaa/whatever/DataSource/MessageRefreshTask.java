package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import mynuaa.whatever.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class MessageRefreshTask extends Task {
	public static final int REFRESH_SUCCESS = 0;
	public static final int REFRESH_FAIL_INNER_ERROR = 1;
	public static final int REFRESH_FAIL_DELETED = 2;

	private final String mCid;
	private final OnMessageRefreshListener mOnMessageRefreshListener;

	private int mResult;
	private MessageData mMessage;

	public MessageRefreshTask(String tag, String cid,
			OnMessageRefreshListener onMessageRefreshListener) {
		super(tag);

		mCid = cid;
		mOnMessageRefreshListener = onMessageRefreshListener;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", UserSession.getCurrentSession().mSession);
		params.put("nid", mCid);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_MESSAGE_GET, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = REFRESH_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http msgfresh", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				JSONObject messageObj = jsonObj.getJSONObject("NewsInfo");

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
				String manner = messageObj.getString("manner");

				int background_color_index_i = Integer
						.parseInt(background_color_index);
				int background_texture_index_i = Integer
						.parseInt(background_texture_index);
				int good_count_i = Integer.parseInt(good_count);
				int bad_count_i = Integer.parseInt(bad_count);
				int comment_count_i = Integer.parseInt(comment_count);
				int manner_i = Integer.parseInt(manner);

				if ("0".equals(image_cid)) {
					image_cid = "";
				}

				time = Util.formatTime(time);

				MessageData md = new MessageData();
				md.cid = cid;
				md.content = content;
				md.image_cid = image_cid;
				md.time = time;
				md.background_color_index = background_color_index_i;
				md.background_texture_index = background_texture_index_i;
				md.good_count = good_count_i;
				md.bad_count = bad_count_i;
				md.comment_count = comment_count_i;
				switch (manner_i) {
				case 1:
					md.put_bad = true;
					break;
				case 2:
					md.put_good = true;
					break;
				}

				mMessage = md;
				updateDatabase();
				mResult = REFRESH_SUCCESS;
			} else if (errorCode == 404) {
				mResult = REFRESH_FAIL_DELETED;
			} else {
				mResult = REFRESH_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = REFRESH_FAIL_INNER_ERROR;
		}
	}

	private void updateDatabase() {
		SQLiteDatabase db = DataCenter.getDatabase(true);

		String selection = DataCenter.DB_MESSAGECACHE_COLUMN_CID + "=?";
		String selectionArgs[] = new String[] { mCid };

		ContentValues cv = new ContentValues();

		cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_GOOD, mMessage.good_count);
		cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_BAD, mMessage.bad_count);
		cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_COMMENT,
				mMessage.comment_count);
		cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_MANNER, (mMessage.put_bad ? 1
				: 0) + (mMessage.put_good ? 2 : 0));

		db.update(DataCenter.DB_MESSAGECACHE_NAME, cv, selection, selectionArgs);
	}

	@Override
	public void callback() {
		if (mOnMessageRefreshListener != null) {
			mOnMessageRefreshListener.onMessageRefresh(mResult, mCid, mMessage);
		}
	}

	public static interface OnMessageRefreshListener {
		public void onMessageRefresh(int result, String cid, MessageData message);
	}

}
