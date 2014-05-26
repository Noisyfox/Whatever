package mynuaa.whatever.DataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class DataCenter {

	public static final String DB_IMAGECACHE_NAME = "image_cache";
	public static final String DB_IMAGECACHE_COLUMN_ID = "iid";
	public static final String DB_IMAGECACHE_COLUMN_CID = "cid";
	public static final String DB_IMAGECACHE_COLUMN_PATH = "path";

	public static final String DB_USER_NAME = "user";
	public static final String DB_USER_COLUMN_SESSION = "session";
	public static final String DB_USER_COLUMN_USERID = "user_id";
	public static final String DB_USER_COLUMN_NAME = "name";
	public static final String DB_USER_COLUMN_PHONE = "phone";
	public static final String DB_USER_COLUMN_HEADCID = "head_cid";

	public static final String DB_MESSAGECACHE_NAME = "message_cache";
	public static final String DB_MESSAGECACHE_COLUMN_ID = "mid";
	public static final String DB_MESSAGECACHE_COLUMN_CID = "cid";
	public static final String DB_MESSAGECACHE_COLUMN_CONTENT = "content";
	public static final String DB_MESSAGECACHE_COLUMN_TIME = "time";
	public static final String DB_MESSAGECACHE_COLUMN_GOOD = "good";
	public static final String DB_MESSAGECACHE_COLUMN_BAD = "bad";
	public static final String DB_MESSAGECACHE_COLUMN_COMMENT = "comment";
	public static final String DB_MESSAGECACHE_COLUMN_IMAGE = "image_cid";
	public static final String DB_MESSAGECACHE_COLUMN_TEXTURE = "texture";
	public static final String DB_MESSAGECACHE_COLUMN_COLOR = "color";
	public static final String DB_MESSAGECACHE_COLUMN_FILTER = "filter";
	public static final String DB_MESSAGECACHE_COLUMN_MANNER = "manner";

	public static final String DB_CONTACTCACHE_NAME = "contact_cache";
	public static final String DB_CONTACTCACHE_COLUMN_DATA = "data";

	public static final String DB_PMCACHE_NAME = "pm_cache";
	public static final String DB_PMCACHE_COLUMN_ID = "pid";
	public static final String DB_PMCACHE_COLUMN_NCID = "ncid";// 这条私信对应的状态ID
	public static final String DB_PMCACHE_COLUMN_CID = "cid";
	public static final String DB_PMCACHE_COLUMN_CONTENT = "content";
	public static final String DB_PMCACHE_COLUMN_SESSION = "session";
	public static final String DB_PMCACHE_COLUMN_TIME = "time";
	public static final String DB_PMCACHE_COLUMN_TIMEL = "timeL";
	public static final String DB_PMCACHE_COLUMN_FROM = "pm_from";// 来自自己还是对方
	public static final String DB_PMCACHE_COLUMN_STATUS = "status";// 消息发送状态

	public static final String DB_NOTICACHE_NAME = "noti_cache";
	public static final String DB_NOTICACHE_COLUMN_ID = "ntid";
	public static final String DB_NOTICACHE_COLUMN_TYPE = "type";
	public static final String DB_NOTICACHE_COLUMN_TIME = "time";
	public static final String DB_NOTICACHE_COLUMN_CONTENT = "content";
	public static final String DB_NOTICACHE_COLUMN_CID = "cid";
	public static final String DB_NOTICACHE_COLUMN_NCID = "ncid";
	public static final String DB_NOTICACHE_COLUMN_COUNT = "count";
	public static final String DB_NOTICACHE_COLUMN_PMSESSION = "pmsession";
	public static final String DB_NOTICACHE_COLUMN_ISREAD = "isread";
	public static final String DB_NOTICACHE_COLUMN_COLOR = "color";
	public static final String DB_NOTICACHE_COLUMN_NOTE = "note";

	protected static Context mContext;
	protected static SQLiteOpenHelper mSQLiteHelper;
	protected static ImageCacheManager mImageCacheManager;
	protected static LocationClient mLocationClient;
	private static MyLocationListener mLocationListener;

	public static void initDataCenter(Context context) {
		Log.d("DataCenter", "DataCenter inited!");
		mContext = context;
		mSQLiteHelper = new MyDataBaseHelper();
		mImageCacheManager = new ImageCacheManager();
		{// 初始化定位服务
			mLocationClient = new LocationClient(context);
			mLocationListener = new MyLocationListener();
			LocationClientOption option = new LocationClientOption();
			option.setLocationMode(LocationMode.Hight_Accuracy);
			option.setCoorType("bd09");
			option.setScanSpan(30000);// 设置发起定位请求的间隔时间为30s
			option.setIsNeedAddress(false);
			option.setNeedDeviceDirect(false);
			mLocationClient.setLocOption(option);
			mLocationClient.registerLocationListener(mLocationListener);
		}
		// hack head image
		// mImageCacheManager.putImageCache("avator",
		// BitmapFactory.decodeResource(
		// context.getResources(), R.drawable.head));
	}

	public static SQLiteDatabase getDatabase(boolean writeable) {
		if (mSQLiteHelper == null) {
			throw new IllegalStateException("DataCenter not inited!");
		}

		return writeable ? mSQLiteHelper.getWritableDatabase() : mSQLiteHelper
				.getReadableDatabase();
	}

	public static void closeAllDatabase() {
		if (mSQLiteHelper == null) {
			throw new IllegalStateException("DataCenter not inited!");
		}
		mSQLiteHelper.close();
	}

	public static ImageCacheManager getImageCacheManager() {
		if (mImageCacheManager == null) {
			throw new IllegalStateException("DataCenter not inited!");
		}
		return mImageCacheManager;
	}

	/*-**********************************************************************-*/

	private static Object mLocSyncObject = new Object();
	private static long mLastLocationUpdateTime = 0;
	private static Location mSavedLocation = null;
	private static List<OnLocationUpdateListener> mOnLocationUpdateListeners = new LinkedList<OnLocationUpdateListener>();

	public static class Location {
		private double mLatitude;
		private double mLongitude;

		public Location(double latitude, double longitude) {
			mLatitude = latitude;
			mLongitude = longitude;
		}

		public double getLatitude() {
			return mLatitude;
		}

		public double getLongitude() {
			return mLongitude;
		}
	}

	public static interface OnLocationUpdateListener {
		public void onLocationUpdate(boolean success, double latitude,
				double longitude);
	}

	private static class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation arg0) {
			int result = arg0.getLocType();
			switch (result) {
			case 66:
			case 61:
			case 65:
			case 68:
			case 161: {
				double latitude = arg0.getLatitude();
				double longitude = arg0.getLongitude();
				Log.d("Location:", "(" + latitude + "," + longitude + ").");
				synchronized (mLocSyncObject) {
					mLastLocationUpdateTime = System.currentTimeMillis();
					if (mSavedLocation == null) {
						mSavedLocation = new Location(latitude, longitude);
					} else {
						mSavedLocation.mLatitude = latitude;
						mSavedLocation.mLongitude = longitude;
					}
					for (OnLocationUpdateListener li : mOnLocationUpdateListeners) {
						li.onLocationUpdate(true, latitude, longitude);
					}
				}
				break;
			}
			case BDLocation.TypeOffLineLocationFail: {
				synchronized (mLocSyncObject) {
					if (mLocationClient.isStarted()) {
						mLocationClient.requestLocation();
					}
				}
				break;
			}
			default:
				synchronized (mLocSyncObject) {
					for (OnLocationUpdateListener li : mOnLocationUpdateListeners) {
						li.onLocationUpdate(false, 0, 0);
					}
				}
			}
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
		}

	}

	public static void registerOnLocationUpdateListener(
			OnLocationUpdateListener onLocationUpdateListener) {
		if (onLocationUpdateListener == null) {
			return;
		}
		synchronized (mLocSyncObject) {
			if (!mOnLocationUpdateListeners.contains(onLocationUpdateListener)) {
				mOnLocationUpdateListeners.add(onLocationUpdateListener);
			}
		}
	}

	public static void unregisterOnLocationUpdateListener(
			OnLocationUpdateListener onLocationUpdateListener) {
		if (onLocationUpdateListener == null) {
			return;
		}
		synchronized (mLocSyncObject) {
			mOnLocationUpdateListeners.remove(onLocationUpdateListener);
		}
	}

	public static void startLoactionService() {
		if (mLocationClient == null) {
			throw new IllegalStateException("DataCenter not inited!");
		}
		synchronized (mLocSyncObject) {
			if (mLocationClient.isStarted())
				return;
			mLocationClient.start();
			if (mSavedLocation == null) {
				mLocationClient.requestOfflineLocation();
			}
		}
	}

	public static void stopLocationService() {
		if (mLocationClient == null) {
			throw new IllegalStateException("DataCenter not inited!");
		}
		synchronized (mLocSyncObject) {
			if (!mLocationClient.isStarted())
				return;
			mLocationClient.stop();
		}
	}

	public static void requireLocation() {
		if (mLocationClient == null) {
			throw new IllegalStateException("DataCenter not inited!");
		}
		synchronized (mLocSyncObject) {
			if (!mLocationClient.isStarted())
				return;
			if (mSavedLocation == null) {
				mLocationClient.requestOfflineLocation();
			} else {
				mLocationClient.requestLocation();
			}
		}
	}

	public static Location requireLocationSync(long timeout) {
		if (mLocationClient == null) {
			throw new IllegalStateException("DataCenter not inited!");
		}
		synchronized (mLocSyncObject) {
			long cTime = System.currentTimeMillis();
			if (cTime - mLastLocationUpdateTime < 15000)
				return getCurrentLocation();
		}

		final OnLocationUpdateListener olul = new OnLocationUpdateListener() {
			@Override
			public void onLocationUpdate(boolean success, double latitude,
					double longitude) {
				synchronized (this) {
					Log.d("LocNotify", "Notify!");
					this.notify();
				}
			}
		};

		synchronized (olul) {
			registerOnLocationUpdateListener(olul);
			requireLocation();
			try {
				olul.wait(timeout);
			} catch (InterruptedException e) {
			}
			unregisterOnLocationUpdateListener(olul);
		}

		return getCurrentLocation();
	}

	public static Location getCurrentLocation() {
		synchronized (mLocSyncObject) {
			return mSavedLocation;
		}
	}

	/*-**********************************************************************-*/

	private static class MyDataBaseHelper extends SQLiteOpenHelper {

		private static final String DB_NAME = "whatever.db";
		private static final int DB_VERSION = 1;

		public MyDataBaseHelper() {
			super(mContext, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = String.format("CREATE TABLE %s(%s, %s, %s, %s, %s);",
					DB_USER_NAME, DB_USER_COLUMN_SESSION,
					DB_USER_COLUMN_USERID, DB_USER_COLUMN_NAME,
					DB_USER_COLUMN_PHONE, DB_USER_COLUMN_HEADCID);
			db.execSQL(sql);

			sql = String
					.format("CREATE TABLE %s(%s integer primary key autoincrement, %s, %s);",
							DB_IMAGECACHE_NAME, DB_IMAGECACHE_COLUMN_ID,
							DB_IMAGECACHE_COLUMN_CID, DB_IMAGECACHE_COLUMN_PATH);
			db.execSQL(sql);

			sql = String
					.format("CREATE TABLE %s(%s integer primary key autoincrement, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);",
							DB_MESSAGECACHE_NAME, DB_MESSAGECACHE_COLUMN_ID,
							DB_MESSAGECACHE_COLUMN_CID,
							DB_MESSAGECACHE_COLUMN_CONTENT,
							DB_MESSAGECACHE_COLUMN_TIME,
							DB_MESSAGECACHE_COLUMN_GOOD,
							DB_MESSAGECACHE_COLUMN_BAD,
							DB_MESSAGECACHE_COLUMN_COMMENT,
							DB_MESSAGECACHE_COLUMN_IMAGE,
							DB_MESSAGECACHE_COLUMN_TEXTURE,
							DB_MESSAGECACHE_COLUMN_COLOR,
							DB_MESSAGECACHE_COLUMN_FILTER,
							DB_MESSAGECACHE_COLUMN_MANNER);
			db.execSQL(sql);

			sql = String.format("CREATE TABLE %s(%s);", DB_CONTACTCACHE_NAME,
					DB_CONTACTCACHE_COLUMN_DATA);
			db.execSQL(sql);

			sql = String
					.format("CREATE TABLE %s(%s integer primary key autoincrement, %s, %s, %s, %s, %s, %s, %s, %s);",
							DB_PMCACHE_NAME, DB_PMCACHE_COLUMN_ID,
							DB_PMCACHE_COLUMN_NCID, DB_PMCACHE_COLUMN_CID,
							DB_PMCACHE_COLUMN_CONTENT,
							DB_PMCACHE_COLUMN_SESSION, DB_PMCACHE_COLUMN_TIME,
							DB_PMCACHE_COLUMN_TIMEL, DB_PMCACHE_COLUMN_FROM,
							DB_PMCACHE_COLUMN_STATUS);
			db.execSQL(sql);

			sql = String
					.format("CREATE TABLE %s(%s integer primary key autoincrement, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);",
							DB_NOTICACHE_NAME, DB_NOTICACHE_COLUMN_ID,
							DB_NOTICACHE_COLUMN_TYPE, DB_NOTICACHE_COLUMN_TIME,
							DB_NOTICACHE_COLUMN_CONTENT,
							DB_NOTICACHE_COLUMN_CID, DB_NOTICACHE_COLUMN_NCID,
							DB_NOTICACHE_COLUMN_COUNT,
							DB_NOTICACHE_COLUMN_PMSESSION,
							DB_NOTICACHE_COLUMN_ISREAD,
							DB_NOTICACHE_COLUMN_COLOR, DB_NOTICACHE_COLUMN_NOTE);
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

	}

	/*-**********************************************************************-*/

	static class ImageCacheManager {

		private static final String PATH_BASE = Environment
				.getExternalStorageDirectory().getPath() + "/whatever";
		private static final String PATH_IMAGE_CACHE = PATH_BASE + "/cache/img";

		public static void saveBitmap(Bitmap bitmap, String filePath,
				String fileName) throws IOException {
			File f = new File(filePath);
			f.mkdirs();
			f = new File(filePath + "/" + fileName);
			f.delete();
			f.createNewFile();
			FileOutputStream fOut = new FileOutputStream(f);

			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

			try {
				fOut.flush();
			} finally {
				fOut.close();
			}

		}

		protected ImageCacheManager() {
		}

		protected void clearImageCache() {
			synchronized (mHotImageCache) {
				mHotImageCache.clear();
				while (!mAllHotImageCache.isEmpty()) {
					ImageCache ic = mAllHotImageCache.poll();
					ic.cacheImage = null;
					ic.cid = null;

					if (mUnusedImageCache.size() < mHotImageCacheMaxCount) {
						mUnusedImageCache.push(ic);
					}
				}
				SQLiteDatabase db = getDatabase(true);
				Cursor c = db.query(DB_IMAGECACHE_NAME, null, null, null, null,
						null, null);
				if (c.moveToFirst()) {
					do {
						String path = c.getString(c
								.getColumnIndex(DB_IMAGECACHE_COLUMN_PATH));
						new File(path).delete();
					} while (c.moveToNext());
				}
				c.close();
				db.delete(DB_IMAGECACHE_NAME, null, null);
				// db.close();
			}
		}

		protected void removeImageCache(String cid, String size) {
			synchronized (mHotImageCache) {
				String size_cid = size + "_" + cid;
				mHotImageCache.remove(size_cid);

				SQLiteDatabase db = getDatabase(true);
				Cursor c = db.query(DB_IMAGECACHE_NAME, null,
						DB_IMAGECACHE_COLUMN_CID + "=?",
						new String[] { size_cid }, null, null, null);
				if (c.moveToFirst()) {
					do {
						String path = c.getString(c
								.getColumnIndex(DB_IMAGECACHE_COLUMN_PATH));
						new File(path).delete();
					} while (c.moveToNext());
				}
				c.close();
				db.delete(DB_IMAGECACHE_NAME, DB_IMAGECACHE_COLUMN_CID + "=?",
						new String[] { size_cid });
				// db.close();
			}
		}

		protected void putImageCache(String cid, Bitmap image, String size) {
			synchronized (mHotImageCache) {
				String size_cid = size + "_" + cid;
				hotImageCache(image, size_cid);

				try {
					String path = PATH_IMAGE_CACHE + "/" + size_cid + ".png";
					saveBitmap(image, PATH_IMAGE_CACHE, size_cid + ".png");
					SQLiteDatabase db = getDatabase(true);
					Cursor c = db.query(DB_IMAGECACHE_NAME, null,
							DB_IMAGECACHE_COLUMN_CID + "=?",
							new String[] { size_cid }, null, null, null);

					ContentValues cv = new ContentValues();
					cv.put(DB_IMAGECACHE_COLUMN_CID, size_cid);
					cv.put(DB_IMAGECACHE_COLUMN_PATH, path);

					if (c.moveToFirst()) {
						String whereClause = DB_IMAGECACHE_COLUMN_CID + "=?";// 修改条件
						String[] whereArgs = { size_cid };// 修改条件的参数
						db.update(DB_IMAGECACHE_NAME, cv, whereClause,
								whereArgs);// 执行修改
					} else {
						db.insert(DB_IMAGECACHE_NAME, null, cv);
					}
					c.close();
					// db.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		protected Bitmap getImageCache(String cid, String size) {
			String size_cid = size + "_" + cid;
			synchronized (mHotImageCache) {
				if (mHotImageCache.containsKey(size_cid))
					return mHotImageCache.get(size_cid).cacheImage;
			}

			SQLiteDatabase db = getDatabase(false);
			Cursor c = db.query(DB_IMAGECACHE_NAME, null,
					DB_IMAGECACHE_COLUMN_CID + "=?", new String[] { size_cid },
					null, null, null);
			Bitmap b = null;
			if (c.moveToFirst()) {
				String path = c.getString(c
						.getColumnIndex(DB_IMAGECACHE_COLUMN_PATH));
				b = BitmapFactory.decodeFile(path);
			}
			if (b != null) {
				hotImageCache(b, size_cid);
			}
			c.close();
			// db.close();
			return b;
		}

		private class ImageCache {
			String cid;
			Bitmap cacheImage;
		}

		private static int mHotImageCacheMaxCount = 50;
		private HashMap<String, ImageCache> mHotImageCache = new HashMap<String, ImageCache>();
		private Queue<ImageCache> mAllHotImageCache = new LinkedList<ImageCache>();
		private Stack<ImageCache> mUnusedImageCache = new Stack<ImageCache>();

		private void hotImageCache(Bitmap image, String cid) {
			synchronized (mHotImageCache) {
				if (mHotImageCache.containsKey(cid)) {
					ImageCache ic = mHotImageCache.get(cid);
					ic.cacheImage = image;
					return;
				}
				if (mAllHotImageCache.size() >= mHotImageCacheMaxCount) {
					ImageCache ic = mAllHotImageCache.poll();
					ic.cacheImage = null;
					mHotImageCache.remove(ic.cid);
					ic.cid = null;
					if (mUnusedImageCache.size() < mHotImageCacheMaxCount / 2) {
						mUnusedImageCache.push(ic);
					}
				}
				ImageCache ic = null;
				if (mUnusedImageCache.isEmpty()) {
					ic = new ImageCache();
				} else {
					ic = mUnusedImageCache.pop();
				}
				ic.cacheImage = image;
				ic.cid = cid;
				mHotImageCache.put(cid, ic);
				mAllHotImageCache.offer(ic);
			}
		}

	}

}
