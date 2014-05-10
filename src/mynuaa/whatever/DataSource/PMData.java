package mynuaa.whatever.DataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mynuaa.whatever.Util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PMData {
	public static final int FROM_ME = 0;
	public static final int FROM_OTHER = 1;
	public static final int FROM_DIVIDER = 2;

	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_SENDING = 1;
	public static final int STATUS_FAIL = 2;

	public String ncid;
	public int cid;
	public String content;
	public String time;
	public String session;
	public int from;// 0-自己 1-别人 2-分隔
	public int status;// 0-正常 1-发送中 2-发送失败

	public long timeL;

	public static class PMHistory {
		private Cursor c;
		private LinkedList<PMData> currData = new LinkedList<PMData>();
		private int length;

		public PMHistory(String session) {
			SQLiteDatabase db = DataCenter.getDatabase(false);

			c = db.query(DataCenter.DB_PMCACHE_NAME, null,
					DataCenter.DB_PMCACHE_COLUMN_SESSION + "=?",
					new String[] { String.valueOf(session) }, null, null,
					DataCenter.DB_PMCACHE_COLUMN_TIMEL);
			length = c.getCount();
		}

		@Override
		protected void finalize() throws Throwable {
			c.close();
			super.finalize();
		}

		public int length() {
			return length;
		}

		/**
		 * 
		 * @param page
		 *            页数，从1开始
		 * @param recordPerPage
		 *            每页条数， 大于0
		 * @return
		 */
		public synchronized List<PMData> loadPage(int page, int recordPerPage) {
			if (page < 1 || recordPerPage < 1) {
				throw new IllegalArgumentException();
			}
			int fromIndex = (page - 1) * recordPerPage;

			currData.clear();

			if (c.moveToPosition(fromIndex)) {
				do {
					PMData pd = new PMData();
					String ncid = c.getString(c
							.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_NCID));
					int cidI = c.getInt(c
							.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_CID));
					String content = c
							.getString(c
									.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_CONTENT));
					String _session = c
							.getString(c
									.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_SESSION));
					String time = c.getString(c
							.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_TIME));
					long timeL = c
							.getLong(c
									.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_TIMEL));
					int from = c.getInt(c
							.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_FROM));
					int status = c
							.getInt(c
									.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_STATUS));

					pd.ncid = ncid;
					pd.cid = cidI;
					pd.content = content;
					pd.session = _session;
					pd.time = time;
					pd.timeL = timeL;
					pd.from = from;
					pd.status = status;

					currData.add(pd);
					recordPerPage--;
				} while (c.moveToNext() && recordPerPage > 0);
			}

			return currData;
		}

		public synchronized List<PMData> getCurrent() {
			return currData;
		}
	}

	public static List<PMData> getCachedMessages(String session, int before) {
		SQLiteDatabase db = DataCenter.getDatabase(false);
		Cursor c;

		if (before < 0) {
			c = db.query(DataCenter.DB_PMCACHE_NAME, null,
					DataCenter.DB_PMCACHE_COLUMN_SESSION + "=?",
					new String[] { String.valueOf(session) }, null, null,
					DataCenter.DB_PMCACHE_COLUMN_TIMEL + " desc", "0, 3");
		} else {
			c = db.query(DataCenter.DB_PMCACHE_NAME, null,
					DataCenter.DB_PMCACHE_COLUMN_SESSION + "=? AND "
							+ DataCenter.DB_PMCACHE_COLUMN_CID + "<" + before,
					new String[] { String.valueOf(session) }, null, null,
					DataCenter.DB_PMCACHE_COLUMN_TIMEL + " desc", "0, 10");
		}

		List<PMData> messages = new ArrayList<PMData>();

		if (c.moveToFirst()) {
			do {
				PMData pd = new PMData();
				String ncid = c.getString(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_NCID));
				int cidI = c.getInt(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_CID));
				String content = c.getString(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_CONTENT));
				String _session = c.getString(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_SESSION));
				String time = c.getString(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_TIME));
				long timeL = c.getLong(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_TIMEL));
				int from = c.getInt(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_FROM));
				int status = c.getInt(c
						.getColumnIndex(DataCenter.DB_PMCACHE_COLUMN_STATUS));

				pd.ncid = ncid;
				pd.cid = cidI;
				pd.content = content;
				pd.session = _session;
				pd.time = time;
				pd.timeL = timeL;
				pd.from = from;
				pd.status = status;

				messages.add(pd);
			} while (c.moveToNext());
		}

		Collections.reverse(messages);

		return messages;
	}

	private static final String PM_COLUMNS[] = new String[] { DataCenter.DB_PMCACHE_COLUMN_CID };
	private static final String PM_SELECTION = DataCenter.DB_PMCACHE_COLUMN_SESSION
			+ "=? AND " + DataCenter.DB_PMCACHE_COLUMN_CID + "=";

	public static void savePMCache(List<PMData> pmData) {
		SQLiteDatabase db = DataCenter.getDatabase(true);
		String selectionArgs[] = new String[] { null };

		for (PMData pd : pmData) {
			selectionArgs[0] = pd.session;

			Cursor c = db.query(DataCenter.DB_PMCACHE_NAME, PM_COLUMNS,
					PM_SELECTION + pd.cid, selectionArgs, null, null, null);

			ContentValues cv = new ContentValues();
			cv.put(DataCenter.DB_PMCACHE_COLUMN_NCID, pd.ncid);
			cv.put(DataCenter.DB_PMCACHE_COLUMN_CID, pd.cid);
			cv.put(DataCenter.DB_PMCACHE_COLUMN_CONTENT, pd.content);
			cv.put(DataCenter.DB_PMCACHE_COLUMN_SESSION, pd.session);
			cv.put(DataCenter.DB_PMCACHE_COLUMN_TIME, pd.time);
			cv.put(DataCenter.DB_PMCACHE_COLUMN_TIMEL, pd.timeL);
			cv.put(DataCenter.DB_PMCACHE_COLUMN_FROM, pd.from);
			cv.put(DataCenter.DB_PMCACHE_COLUMN_STATUS, pd.status);

			if (!c.moveToFirst()) {
				db.insert(DataCenter.DB_PMCACHE_NAME, null, cv);
			} else {
				db.update(DataCenter.DB_PMCACHE_NAME, cv,
						PM_SELECTION + pd.cid, selectionArgs);
			}

			c.close();
		}
	}

	public static void savePMCache(PMData pmData) {
		SQLiteDatabase db = DataCenter.getDatabase(true);
		String selectionArgs[] = new String[] { pmData.session };

		Cursor c = db.query(DataCenter.DB_PMCACHE_NAME, PM_COLUMNS,
				PM_SELECTION + pmData.cid, selectionArgs, null, null, null);

		ContentValues cv = new ContentValues();
		cv.put(DataCenter.DB_PMCACHE_COLUMN_NCID, pmData.ncid);
		cv.put(DataCenter.DB_PMCACHE_COLUMN_CID, pmData.cid);
		cv.put(DataCenter.DB_PMCACHE_COLUMN_CONTENT, pmData.content);
		cv.put(DataCenter.DB_PMCACHE_COLUMN_SESSION, pmData.session);
		cv.put(DataCenter.DB_PMCACHE_COLUMN_TIME, pmData.time);
		cv.put(DataCenter.DB_PMCACHE_COLUMN_TIMEL, pmData.timeL);
		cv.put(DataCenter.DB_PMCACHE_COLUMN_FROM, pmData.from);
		cv.put(DataCenter.DB_PMCACHE_COLUMN_STATUS, pmData.status);

		if (!c.moveToFirst()) {
			db.insert(DataCenter.DB_PMCACHE_NAME, null, cv);
		} else {
			db.update(DataCenter.DB_PMCACHE_NAME, cv,
					PM_SELECTION + pmData.cid, selectionArgs);
		}

		c.close();
	}

	public static PMData createTimeDivider(String ncid, long time) {
		PMData pd = new PMData();
		pd.timeL = time;
		pd.ncid = ncid;
		pd.from = FROM_DIVIDER;
		pd.time = Util.getLeveledTime(time);

		return pd;
	}

	@Override
	public boolean equals(Object o) {
		if (!PMData.class.isInstance(o)) {
			return false;
		}
		PMData po = (PMData) o;
		if (!po.ncid.equals(ncid)) {
			return false;
		}
		if (po.from != from) {
			return false;
		}
		if (from == FROM_DIVIDER) {
			return po.timeL == timeL;
		}
		if (cid < 0 || po.cid < 0) {
			return false;
		}

		return cid == po.cid;
	}
}
