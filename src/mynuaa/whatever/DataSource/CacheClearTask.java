package mynuaa.whatever.DataSource;

import android.database.sqlite.SQLiteDatabase;
import mynuaa.whatever.DataSource.DataCenter.ImageCacheManager;

public class CacheClearTask extends Task {

	private final OnCacheClearListener mOnCacheClearListener;

	public CacheClearTask(String tag, OnCacheClearListener onCacheClearListener) {
		super(tag);
		mOnCacheClearListener = onCacheClearListener;
	}

	@Override
	public void doTask() {
		clearCache();
	}
	
	public static void clearCache(){
		// Çå³ýÍ¼Æ¬»º´æ
		ImageCacheManager icm = DataCenter.getImageCacheManager();
		icm.clearImageCache();
		// Çå³ý×´Ì¬»º´æ
		SQLiteDatabase db = DataCenter.getDatabase(true);
		db.delete(DataCenter.DB_MESSAGECACHE_NAME, null, null);
		db.delete(DataCenter.DB_PMCACHE_NAME, null, null);
		db.delete(DataCenter.DB_CONTACTCACHE_NAME, null, null);
		// db.close();
	}

	@Override
	public void callback() {
		if (mOnCacheClearListener != null) {
			mOnCacheClearListener.onCacheClear();
		}
	}

	public static interface OnCacheClearListener {
		public void onCacheClear();
	}
}
