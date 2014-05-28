package mynuaa.whatever.DataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import mynuaa.whatever.Util;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class ContactSyncTask extends Task {

	public static final int SYNC_SUCCESS = 0;
	public static final int SYNC_FAIL_INNER_ERROR = 1;

	private final OnContactSyncListener mOnContactSyncListener;
	private final List<String> mContacts;

	private int mResult = SYNC_SUCCESS;

	public ContactSyncTask(String tag, Context context,
			OnContactSyncListener onContactSyncListener) {
		super(tag);

		mOnContactSyncListener = onContactSyncListener;
		mContacts = Util.getContactAllPhoneNumber(context);
	}

	@Override
	public void doTask() {

		JSONArray ja = new JSONArray();
		for (String c : mContacts) {
			ja.put(c);
		}
		String jaS = ja.toString();

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("contact", jaS);

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_CONTACT_SYNC, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = SYNC_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http consync", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode != 0) {
				mResult = SYNC_FAIL_INNER_ERROR;
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = SYNC_FAIL_INNER_ERROR;
			return;
		}

		SQLiteDatabase db = DataCenter.getDatabase(true);

		db.delete(DataCenter.DB_CONTACTCACHE_NAME, null, null);

		for (String c : mContacts) {
			ContentValues cv = new ContentValues();
			cv.put(DataCenter.DB_CONTACTCACHE_COLUMN_DATA, c);
			db.insert(DataCenter.DB_CONTACTCACHE_NAME, null, cv);
		}

		mResult = SYNC_SUCCESS;

	}

	@Override
	public void callback() {
		if (mOnContactSyncListener != null) {
			mOnContactSyncListener.onContactSync(mResult);
		}
	}

	public static boolean checkContactChange(Context context) {
		List<String> realContacts = Util.getContactAllPhoneNumber(context);
		Collections.sort(realContacts);

		SQLiteDatabase db = DataCenter.getDatabase(false);

		Cursor c = db.query(DataCenter.DB_CONTACTCACHE_NAME,
				new String[] { DataCenter.DB_CONTACTCACHE_COLUMN_DATA }, null,
				null, null, null, DataCenter.DB_CONTACTCACHE_COLUMN_DATA);

		List<String> cachedContacts = new ArrayList<String>();
		if (c.moveToFirst()) {
			do {
				cachedContacts
						.add(c.getString(c
								.getColumnIndex(DataCenter.DB_CONTACTCACHE_COLUMN_DATA)));
			} while (c.moveToNext());
		}

		int length = realContacts.size();
		if (length != cachedContacts.size())
			return false;

		if (length == 0)
			return true;

		String contactsR[] = new String[length];
		String contactsC[] = new String[length];

		realContacts.toArray(contactsR);
		cachedContacts.toArray(contactsC);

		for (int i = 0; i < length; i++) {
			if (!contactsR[i].equals(contactsC[i])) {
				return false;
			}
		}

		return true;
	}

	public static boolean contactEnabled(Context context) {
		SharedPreferences sp = context.getSharedPreferences("Settings",
				Context.MODE_PRIVATE);
		return sp.getBoolean("autoContact", false);
	}

	public static interface OnContactSyncListener {
		public void onContactSync(int result);
	}

}
