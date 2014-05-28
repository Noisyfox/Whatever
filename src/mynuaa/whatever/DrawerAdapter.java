package mynuaa.whatever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerAdapter extends BaseAdapter {

	public class DrawerItem {
		String title;
		int id;
		Bitmap icon;
		boolean hasNoti = false;
	}

	private List<DrawerItem> mItems = new ArrayList<DrawerItem>();

	/** Menu tag name in XML. */
	private static final String XML_MENU = "drawer_menu";

	/** Item tag name in XML. */
	private static final String XML_ITEM = "item";

	private Context mContext;

	public DrawerAdapter(Context context, int resId) {
		mContext = context;
		XmlResourceParser parser = null;
		try {
			parser = mContext.getResources().getLayout(resId);
			AttributeSet attrs = Xml.asAttributeSet(parser);

			parseDrawer(parser, attrs);
		} catch (XmlPullParserException e) {
			throw new InflateException("Error inflating drawer XML", e);
		} catch (IOException e) {
			throw new InflateException("Error inflating drawer XML", e);
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	private void parseDrawer(XmlPullParser parser, AttributeSet attrs)
			throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String tagName;
		boolean lookingForEndOfUnknownTag = false;
		String unknownTagName = null;

		// This loop will skip to the menu start tag
		do {
			if (eventType == XmlPullParser.START_TAG) {
				tagName = parser.getName();
				if (tagName.equals(XML_MENU)) {
					// Go to next tag
					eventType = parser.next();
					break;
				}

				throw new RuntimeException("Expecting drawer_menu, got "
						+ tagName);
			}
			eventType = parser.next();
		} while (eventType != XmlPullParser.END_DOCUMENT);

		boolean reachedEndOfMenu = false;
		while (!reachedEndOfMenu) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (lookingForEndOfUnknownTag) {
					break;
				}

				tagName = parser.getName();
				if (tagName.equals(XML_ITEM)) {
					DrawerItem item = new DrawerItem();
					TypedArray a = mContext.obtainStyledAttributes(attrs,
							R.styleable.DrawerDef);

					int id = a
							.getResourceId(R.styleable.DrawerDef_drawer_id, 0);
					String title = a
							.getString(R.styleable.DrawerDef_drawer_title);
					Drawable d = a
							.getDrawable(R.styleable.DrawerDef_drawer_icon);
					IllegalArgumentException ex = null;
					if (id == 0 || title == null || d == null) {
						ex = new IllegalArgumentException(
								"The content attribute is required.");
					}
					a.recycle();
					if (ex != null)
						throw ex;

					item.id = id;
					item.title = title;
					item.icon = drawableToBitmap(d);

					mItems.add(item);
				} else {
					lookingForEndOfUnknownTag = true;
					unknownTagName = tagName;
				}
				break;

			case XmlPullParser.END_TAG:
				tagName = parser.getName();
				if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
					lookingForEndOfUnknownTag = false;
					unknownTagName = null;
				} else if (tagName.equals(XML_ITEM)) {

				} else if (tagName.equals(XML_MENU)) {
					reachedEndOfMenu = true;
				}
				break;

			case XmlPullParser.END_DOCUMENT:
				throw new RuntimeException("Unexpected end of document");
			}

			eventType = parser.next();
		}

	}

	public void setTitle(int id, String title) {
		for (DrawerItem di : mItems) {
			if (di.id == id) {
				di.title = title;
				break;
			}
		}
		notifyDataSetChanged();
	}

	public void setIcon(int id, Bitmap icon) {
		for (DrawerItem di : mItems) {
			if (di.id == id) {
				di.icon = icon;
				break;
			}
		}
		notifyDataSetChanged();
	}

	public void setNotification(int id, boolean hasNoti) {
		for (DrawerItem di : mItems) {
			if (di.id == id) {
				di.hasNoti = hasNoti;
				break;
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItems.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.drawer_list_item, null);

		MaskedImageView di = (MaskedImageView) convertView
				.findViewById(R.id.drawerImage);
		TextView tv = (TextView) convertView.findViewById(R.id.drawerText);

		DrawerItem i = mItems.get(position);

		tv.setText(i.title);
		di.setIconBitmap(i.icon);
		if (i.hasNoti) {
			di.setStampId(R.drawable.drawer_stamp_notify);
		} else {
			di.setStampId(R.drawable.drawer_stamp);
		}
		convertView.setId(i.id);

		return convertView;
	}

	private static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap.createBitmap(

		drawable.getIntrinsicWidth(),

		drawable.getIntrinsicHeight(),

		drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

		: Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);

		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		drawable.draw(canvas);

		return bitmap;

	}

}
