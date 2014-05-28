package mynuaa.whatever.DataSource;

import mynuaa.whatever.Util;
import mynuaa.whatever.DataSource.DataCenter.ImageCacheManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

public class UserSession {

	private static UserSession mCurrentSession = null;

	protected UserInfo mUserInfo = new UserInfo();

	protected String mSession;
	protected int mUnreadNotificationCount;

	protected UserSession() {
	}

	public static UserSession getCurrentSession() {
		return mCurrentSession;
	}

	protected static void setCurrentSession(UserSession session) {
		mCurrentSession = session;
	}

	public static void clearLocalSession(Context context) {
		UserSession us = loadLocalSession(context);

		SQLiteDatabase db = DataCenter.getDatabase(true);
		db.delete(DataCenter.DB_USER_NAME, null, null);
		// db.close();

		if (us != null) {
			ImageCacheManager icm = DataCenter.getImageCacheManager();
			icm.removeImageCache(us.mUserInfo.mHead, "big");
			icm.removeImageCache(us.mUserInfo.mHead + "_blur", "big");
		}
	}

	public static UserSession loadLocalSession(Context context) {
		SQLiteDatabase db = DataCenter.getDatabase(false);
		Cursor c = db.query(DataCenter.DB_USER_NAME, null, null, null, null,
				null, null);
		UserSession us = null;

		if (c.moveToFirst()) {
			String session, user_id, name, phone, head;
			session = c.getString(c
					.getColumnIndex(DataCenter.DB_USER_COLUMN_SESSION));
			user_id = c.getString(c
					.getColumnIndex(DataCenter.DB_USER_COLUMN_USERID));
			name = c.getString(c.getColumnIndex(DataCenter.DB_USER_COLUMN_NAME));
			phone = c.getString(c
					.getColumnIndex(DataCenter.DB_USER_COLUMN_PHONE));
			head = c.getString(c
					.getColumnIndex(DataCenter.DB_USER_COLUMN_HEADCID));
			int noti = c.getInt(c
					.getColumnIndex(DataCenter.DB_USER_COLUMN_NOTI_UNREAD));

			UserSession _us = new UserSession();
			_us.mSession = session;
			_us.mUnreadNotificationCount = noti;
			_us.mUserInfo.mUid = user_id;
			_us.mUserInfo.mName = name;
			_us.mUserInfo.mPhone = phone;
			_us.mUserInfo.mHead = head;

			us = _us;
		}

		c.close();
		// db.close();

		mCurrentSession = us;

		return mCurrentSession;
	}

	public void addUnreadNotification(int count) {
		mUnreadNotificationCount += count;
	}

	public void cleadUnreadNotification() {
		mUnreadNotificationCount = 0;
	}

	public int getUnreadNotificationCount() {
		return mUnreadNotificationCount;
	}

	public UserInfo getUserInfo() {
		return mUserInfo;
	}

	public void saveAsLocalSession(Context context) {
		SQLiteDatabase db = DataCenter.getDatabase(true);
		Cursor c = db.query(DataCenter.DB_USER_NAME, null, null, null, null,
				null, null);

		ContentValues cv = new ContentValues();
		cv.put(DataCenter.DB_USER_COLUMN_SESSION, mSession);
		cv.put(DataCenter.DB_USER_COLUMN_NOTI_UNREAD, mUnreadNotificationCount);
		cv.put(DataCenter.DB_USER_COLUMN_USERID, mUserInfo.mUid);
		cv.put(DataCenter.DB_USER_COLUMN_NAME, mUserInfo.mName);
		cv.put(DataCenter.DB_USER_COLUMN_PHONE, mUserInfo.mPhone);
		cv.put(DataCenter.DB_USER_COLUMN_HEADCID, mUserInfo.mHead);

		if (c.moveToFirst()) {
			db.update(DataCenter.DB_USER_NAME, cv, null, null);// Ö´ÐÐÐÞ¸Ä
		} else {
			db.insert(DataCenter.DB_USER_NAME, null, cv);
		}
		c.close();
		// db.close();
	}

	public class UserInfo {
		protected String mUid, mName, mPhone, mHead;
		protected Bitmap mHead_img, mHead_blur_img;

		public String getUid() {
			return mUid;
		}

		public String getName() {
			return mName;
		}

		public String getPhone() {
			return mPhone;
		}

		protected void loadHeadBitmap(boolean checkCache) {
			mHead_img = ImageLoadTask.loadBitmap(mHead, checkCache, "big");
			if (mHead_img == null) {
				mHead_blur_img = null;
			} else {
				String blur = mHead + "_blur";
				mHead_blur_img = DataCenter.getImageCacheManager()
						.getImageCache(blur, "big");
				if (mHead_blur_img == null) {
					mHead_blur_img = Util.blurHeadBackground(mHead_img);
					DataCenter.getImageCacheManager().putImageCache(blur,
							mHead_blur_img, "big");
				}
			}
		}

		public Bitmap getHeadBitmap() {
			return mHead_img;
		}

		public Bitmap getHeadBlurBitmap() {
			return mHead_blur_img;
		}
	}

}
