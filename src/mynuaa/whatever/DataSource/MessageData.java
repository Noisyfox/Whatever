package mynuaa.whatever.DataSource;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MessageData implements Parcelable, Comparable<MessageData> {
	public String cid;
	public String content;
	public String image_cid;
	public String time_normative;
	public long time_precise;
	public int background_color_index;
	public int background_texture_index;
	public int good_count, bad_count, comment_count;
	public boolean put_good, put_bad;
	public String select_id;
	public boolean is_me;

	public Bitmap image;
	public Bitmap image_prev;

	public static final int MESSAGE_FILTER_CONTACT = 0;
	public static final int MESSAGE_FILTER_LOCATION = 1;

	public boolean image_load_fail = false;

	public static List<MessageData> getCachedMessages(int messageFilter) {
		SQLiteDatabase db = DataCenter.getDatabase(false);
		Cursor c = db.query(DataCenter.DB_MESSAGECACHE_NAME, null,
				DataCenter.DB_MESSAGECACHE_COLUMN_FILTER + "=?",
				new String[] { String.valueOf(messageFilter) }, null, null,
				DataCenter.DB_MESSAGECACHE_COLUMN_TIME_PREC + " desc"/*
																	 * 0rderBy
																	 */,
				"0, 20" /* limit */);

		List<MessageData> messages = new ArrayList<MessageData>();
		if (c.moveToFirst()) {
			do {
				MessageData md = new MessageData();
				String cid = c.getString(c
						.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_CID));
				String content = c
						.getString(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_CONTENT));
				String image_cid = c
						.getString(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_IMAGE));
				String time_norm = c
						.getString(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_TIME_NORM));
				long time_prec = c
						.getLong(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_TIME_PREC));

				int background_color_index = c
						.getInt(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_COLOR));
				int background_texture_index = c
						.getInt(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_TEXTURE));
				int good_count = c
						.getInt(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_GOOD));
				int bad_count = c.getInt(c
						.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_BAD));
				int comment_count = c
						.getInt(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_COMMENT));
				int manner = c
						.getInt(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_MANNER));

				int is_me = c
						.getInt(c
								.getColumnIndex(DataCenter.DB_MESSAGECACHE_COLUMN_ISME));

				if ("0".equals(image_cid)) {
					image_cid = "";
				}

				md.cid = cid;
				md.content = content;
				md.image_cid = image_cid;
				md.time_normative = time_norm;
				md.time_precise = time_prec;
				md.background_color_index = background_color_index;
				md.background_texture_index = background_texture_index;
				md.good_count = good_count;
				md.bad_count = bad_count;
				md.comment_count = comment_count;
				md.put_bad = false;
				md.put_good = false;
				switch (manner) {
				case 1:
					md.put_bad = true;
					break;
				case 2:
					md.put_good = true;
					break;
				}
				md.is_me = is_me == 1;

				messages.add(md);
			} while (c.moveToNext());
		}

		c.close();
		// db.close();
		return messages;
	}

	protected static void saveMessageCache(List<MessageData> messages,
			int messageFilter) {
		SQLiteDatabase db = DataCenter.getDatabase(true);

		String columns[] = new String[] { DataCenter.DB_MESSAGECACHE_COLUMN_CID };
		String selection = DataCenter.DB_MESSAGECACHE_COLUMN_CID + "=? AND "
				+ DataCenter.DB_MESSAGECACHE_COLUMN_FILTER + "=?";
		String selectionArgs[] = new String[] { null, null };

		for (MessageData m : messages) {
			selectionArgs[0] = m.cid;
			selectionArgs[1] = String.valueOf(messageFilter);
			Cursor c = db.query(DataCenter.DB_MESSAGECACHE_NAME, columns,
					selection, selectionArgs, null, null, null);

			ContentValues cv = new ContentValues();

			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_CID, m.cid);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_CONTENT, m.content);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_TIME_NORM,
					m.time_normative);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_TIME_PREC, m.time_precise);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_GOOD, m.good_count);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_BAD, m.bad_count);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_COMMENT, m.comment_count);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_IMAGE, m.image_cid);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_TEXTURE,
					m.background_texture_index);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_COLOR,
					m.background_color_index);
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_FILTER,
					String.valueOf(messageFilter));
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_MANNER,
					(m.put_bad ? 1 : 0) + (m.put_good ? 2 : 0));
			cv.put(DataCenter.DB_MESSAGECACHE_COLUMN_ISME, m.is_me ? 1 : 0);

			if (!c.moveToFirst()) {
				db.insert(DataCenter.DB_MESSAGECACHE_NAME, null, cv);
			} else {
				db.update(DataCenter.DB_MESSAGECACHE_NAME, cv, selection,
						selectionArgs);
			}

			c.close();
		}

		// db.close();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(cid);
		dest.writeString(content);
		dest.writeString(image_cid);
		dest.writeString(time_normative);
		dest.writeLong(time_precise);
		dest.writeInt(background_color_index);
		dest.writeInt(background_texture_index);
		dest.writeInt(good_count);
		dest.writeInt(bad_count);
		dest.writeInt(comment_count);
		dest.writeBooleanArray(new boolean[] { put_good, put_bad, is_me });
	}

	public static final Parcelable.Creator<MessageData> CREATOR = new Parcelable.Creator<MessageData>() {

		@Override
		public MessageData createFromParcel(Parcel source) {
			MessageData md = new MessageData();
			md.cid = source.readString();
			md.content = source.readString();
			md.image_cid = source.readString();
			md.time_normative = source.readString();
			md.time_precise = source.readLong();
			md.background_color_index = source.readInt();
			md.background_texture_index = source.readInt();
			md.good_count = source.readInt();
			md.bad_count = source.readInt();
			md.comment_count = source.readInt();
			boolean _b[] = new boolean[3];
			source.readBooleanArray(_b);
			md.put_good = _b[0];
			md.put_bad = _b[1];
			md.is_me = _b[2];

			return md;
		}

		@Override
		public MessageData[] newArray(int size) {
			return new MessageData[size];
		}

	};

	@Override
	public int compareTo(MessageData another) {
		long l1 = Long.parseLong(cid);
		long l2 = Long.parseLong(another.cid);
		return l1 == l2 ? 0 : (l1 > l2 ? -1 : 1);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (MessageData.class.isInstance(o)) {
			return ((MessageData) o).cid.equals(cid);
		}
		return super.equals(o);
	}
}
