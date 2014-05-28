package mynuaa.whatever.DataSource;

import android.database.sqlite.SQLiteDatabase;

public class PMHistoryDeleteTask extends Task {

	private final OnHistoryDeleteListener mOnCacheClearListener;
	private final String mPMSession;

	public PMHistoryDeleteTask(String tag, String pmSession,
			OnHistoryDeleteListener onHistoryDeleteListener) {
		super(tag);
		mPMSession = pmSession;
		mOnCacheClearListener = onHistoryDeleteListener;
	}

	@Override
	public void doTask() {
		SQLiteDatabase db = DataCenter.getDatabase(true);
		db.delete(DataCenter.DB_PMCACHE_NAME,
				DataCenter.DB_PMCACHE_COLUMN_SESSION + "=?",
				new String[] { mPMSession });
	}

	@Override
	public void callback() {
		if (mOnCacheClearListener != null) {
			mOnCacheClearListener.onHistoryDelete(mPMSession);
		}
	}

	public static interface OnHistoryDeleteListener {
		public void onHistoryDelete(String session);
	}
}
