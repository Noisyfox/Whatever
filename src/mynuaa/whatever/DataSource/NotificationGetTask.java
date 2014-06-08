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

public class NotificationGetTask extends Task {

	public static final int GET_SUCCESS = 0;
	public static final int GET_FAIL_INNER_ERROR = 1;

	public static final int TYPE_UNREAD = 0;
	public static final int TYPE_READ = 1;
	public static final int TYPE_ALL = 2;

	private static final String NOTYPE_COMMENT = "comment";
	private static final String NOTYPE_BADS = "bads";
	private static final String NOTYPE_GOODS = "goods";
	private static final String NOTYPE_REPORT = "report";
	private static final String NOTYPE_PM = "pm";
	private static final String NOTYPE_WHO = "who";
	private static final String NOTYPE_WHO_REPLY = "who_reply";
	private static final String NOTYPES[] = { NOTYPE_GOODS, NOTYPE_BADS,
			NOTYPE_COMMENT, NOTYPE_PM, NOTYPE_REPORT, NOTYPE_WHO };

	private final OnNotificationGetListener mOnNotificationGetListener;
	private final int mReadType;
	private final String mLastId;
	private final String mType;
	private final int mTypeI;

	private int mResult = GET_SUCCESS;
	private List<NotificationData> mNds;

	public NotificationGetTask(String tag, int readType, int notifyType,
			OnNotificationGetListener l) {
		super(tag);

		mReadType = readType;
		mOnNotificationGetListener = l;
		mLastId = null;

		if (notifyType < 0 || notifyType >= NOTYPES.length) {
			mType = "all";
			mTypeI = -1;
		} else {
			mType = NOTYPES[notifyType];
			mTypeI = notifyType;
		}
	}

	public NotificationGetTask(String tag, int notifyType,
			OnNotificationGetListener l) {
		super(tag);

		mReadType = TYPE_UNREAD;
		mOnNotificationGetListener = l;
		mLastId = null;

		if (notifyType < 0 || notifyType >= NOTYPES.length) {
			mType = "all";
			mTypeI = -1;
		} else {
			mType = NOTYPES[notifyType];
			mTypeI = notifyType;
		}
	}

	public NotificationGetTask(String tag, int notifyType, String lastId,
			OnNotificationGetListener l) {
		super(tag);

		mReadType = TYPE_READ;
		mOnNotificationGetListener = l;
		mLastId = lastId;

		if (notifyType < 0 || notifyType >= NOTYPES.length) {
			mType = "all";
			mTypeI = -1;
		} else {
			mType = NOTYPES[notifyType];
			mTypeI = notifyType;
		}
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("type", mType);
		params.put("isread", String.valueOf(mReadType));
		if (mLastId != null) {
			params.put("last_noticeid", mLastId);
		}
		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_NOTIFY_GET, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = GET_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http notiget", result);
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

			JSONArray ja = jsonObj.getJSONArray("NotificationList");

			ArrayList<NotificationData> notis = new ArrayList<NotificationData>();

			int size = ja.length();
			for (int i = 0; i < size; i++) {
				JSONObject notifyObj = ja.getJSONObject(i);

				String content = notifyObj.getString("content");
				String note = notifyObj.getString("note");
				String time = notifyObj.getString("time");
				String count = notifyObj.getString("count");
				String typeId = notifyObj.getString("typeid");
				String type = notifyObj.getString("type");
				String cid = notifyObj.getString("notice_id");
				String ncid = notifyObj.getString("nid");
				String color = notifyObj.getString("color");
				String isRead = notifyObj.getString("isread");
				String ext = notifyObj.getString("ext");

				int count_i = Integer.parseInt(count);
				int color_i = Integer.parseInt(color);

				NotificationData nd = new NotificationData();

				if (NOTYPE_COMMENT.equals(type)) {
					nd.type = NotificationData.TYPE_COMMENT;
				} else if (NOTYPE_BADS.equals(type)) {
					nd.type = NotificationData.TYPE_BAD;
				} else if (NOTYPE_GOODS.equals(type)) {
					nd.type = NotificationData.TYPE_GOOD;
				} else if (NOTYPE_PM.equals(type)) {
					nd.type = NotificationData.TYPE_PM;
				} else if (NOTYPE_REPORT.equals(type)) {
					nd.type = NotificationData.TYPE_REPORT;
				} else if (NOTYPE_WHO.equals(type)) {
					nd.type = NotificationData.TYPE_WHO;
				} else if (NOTYPE_WHO_REPLY.equals(type)) {
					nd.type = NotificationData.TYPE_WHO_REPLY;
				} else {
					throw new RuntimeException("Known notification type:"
							+ type);
				}

				nd.time = time;
				nd.content = Util.messageDecode(content);
				nd.note = Util.messageDecode(note);
				nd.cid = cid;
				nd.ncid = ncid;
				nd.count = count_i;
				nd.pmsession = typeId;
				nd.isRead = String.valueOf(TYPE_READ).equals(isRead);
				nd.theme_color = color_i;
				nd.ext = ext;

				notis.add(nd);
			}

			NotificationData.saveNotificationCache(notis);
			mNds = notis;
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = GET_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnNotificationGetListener != null) {
			mOnNotificationGetListener.onNotificationGet(mResult, mReadType,
					mTypeI, mNds);
		}
	}

	public static interface OnNotificationGetListener {
		public void onNotificationGet(int result, int readType, int notifyType,
				List<NotificationData> nds);
	}

}
