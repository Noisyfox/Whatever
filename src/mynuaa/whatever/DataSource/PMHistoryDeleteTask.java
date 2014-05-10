package mynuaa.whatever.DataSource;

import android.database.sqlite.SQLiteDatabase;

public class PMHistoryDeleteTask extends Task {

	private final OnHistoryDeleteListener mOnCacheClearListener;
	private final String mSession;

	public PMHistoryDeleteTask(String tag, String session,
			OnHistoryDeleteListener onHistoryDeleteListener) {
		super(tag);
		mSession = session;
		mOnCacheClearListener = onHistoryDeleteListener;
	}

	@Override
	public void doTask() {
		SQLiteDatabase db = DataCenter.getDatabase(true);
		db.delete(DataCenter.DB_PMCACHE_NAME,
				DataCenter.DB_PMCACHE_COLUMN_SESSION + "=?",
				new String[] { mSession });
	}

	@Override
	public void callback() {
		if (mOnCacheClearListener != null) {
			mOnCacheClearListener.onHistoryDelete(mSession);
		}
	}

	public static interface OnHistoryDeleteListener {
		public void onHistoryDelete(String session);
	}
}
