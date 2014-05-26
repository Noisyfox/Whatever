package mynuaa.whatever.DataSource;

import java.util.ArrayList;
import java.util.List;

import mynuaa.whatever.Util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NotificationData {
	public static final int TYPE_GOOD = 1;
	public static final int TYPE_BAD = 2;
	public static final int TYPE_COMMENT = 3;
	public static final int TYPE_PM = 4;
	public static final int TYPE_REPORT = 5;
	public static final int TYPE_WHO = 6;

	public int type;
	public String time;
	public String content;
	public String note;
	public String cid;// noti cid
	public String ncid;// news cid
	public int count;
	public String pmsession;
	public boolean isRead;
	public int theme_color;

	public static List<NotificationData> getUnreadNotification() {
		SQLiteDatabase db = DataCenter.getDatabase(false);

		Cursor c = db.query(DataCenter.DB_NOTICACHE_NAME, null,
				DataCenter.DB_NOTICACHE_COLUMN_ISREAD + "=0", null, null, null,
				DataCenter.DB_NOTICACHE_COLUMN_TIME + " Desc");

		ArrayList<NotificationData> nds = new ArrayList<NotificationData>();

		if (c.moveToFirst()) {
			do {
				NotificationData nd = readFromCursor(c);
				nds.add(nd);
			} while (c.moveToNext());
		}

		c.close();

		return nds;
	}

	public static void saveNotificationCache(List<NotificationData> nds) {
		SQLiteDatabase db = DataCenter.getDatabase(true);

		String selection = DataCenter.DB_NOTICACHE_COLUMN_CID + "=?";
		String selectionArgs[] = new String[] { null };

		for (NotificationData nd : nds) {
			selectionArgs[0] = nd.cid;

			Cursor c = db.query(DataCenter.DB_NOTICACHE_NAME, null, selection,
					selectionArgs, null, null, null);

			ContentValues cv = new ContentValues();

			cv.put(DataCenter.DB_NOTICACHE_COLUMN_TYPE, nd.type);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_TIME, nd.time);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_CONTENT, nd.content);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_CID, nd.cid);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_NCID, nd.ncid);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_COUNT, nd.count);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_PMSESSION, nd.pmsession);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_ISREAD, nd.isRead ? 1 : 0);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_COLOR, nd.theme_color);
			cv.put(DataCenter.DB_NOTICACHE_COLUMN_NOTE, nd.note);

			if (!c.moveToFirst()) {
				db.insert(DataCenter.DB_NOTICACHE_NAME, null, cv);
			} else {
				db.update(DataCenter.DB_NOTICACHE_NAME, cv, selection,
						selectionArgs);
			}

			c.close();
		}
	}

	public static void setRead(String cid) {
		SQLiteDatabase db = DataCenter.getDatabase(true);

		ContentValues cv = new ContentValues();
		cv.put(DataCenter.DB_NOTICACHE_COLUMN_ISREAD, 1);

		db.update(DataCenter.DB_NOTICACHE_NAME, cv,
				DataCenter.DB_NOTICACHE_COLUMN_CID + "=?", new String[] { cid });
	}

	private static NotificationData readFromCursor(Cursor c) {
		int type = c.getInt(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_TYPE));
		String time = c.getString(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_TIME));
		String content = c.getString(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_CONTENT));
		String cid = c.getString(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_CID));
		String ncid = c.getString(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_NCID));
		int count = c.getInt(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_COUNT));
		String pmsession = c.getString(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_PMSESSION));
		int isRead_i = c.getInt(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_ISREAD));
		int color = c.getInt(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_COLOR));
		String note = c.getString(c
				.getColumnIndex(DataCenter.DB_NOTICACHE_COLUMN_NOTE));

		NotificationData nd = new NotificationData();

		nd.type = type;
		nd.time = time;
		nd.content = Util.messageDecode(content);
		nd.cid = cid;
		nd.ncid = ncid;
		nd.count = count;
		nd.pmsession = pmsession;
		nd.isRead = isRead_i == 1;
		nd.theme_color = color;
		nd.note = note;

		return nd;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!NotificationData.class.isInstance(o)) {
			return false;
		}

		NotificationData nd = (NotificationData) o;

		return cid.equals(nd.cid);
	}

}
