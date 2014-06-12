package mynuaa.whatever.SettingsWidget;

import mynuaa.whatever.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class ButtonSetting extends Setting {
	public ButtonSetting(String title, String id) {
		super(title, id);
	}

	@Override
	public void load(SharedPreferences sp) {
	}

	@Override
	public void save(Editor e) {
	}

	private String mNotify = null;

	@Override
	public View getView(Context context, View arg1, ViewGroup arg2) {
		LinearLayout ll = (LinearLayout) arg1
				.findViewById(R.id.content_container);

		ll.removeAllViews();

		if (mNotify == null) {
			ImageView iv = new ImageView(context);
			iv.setImageResource(R.drawable.ic_action_next_item);
			iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT));
			iv.setAdjustViewBounds(true);
			ll.addView(iv);
		} else {
			TextView mTextView = new TextView(context);
			mTextView.setTextColor(0xff3581C2);
			mTextView.setText(mNotify);
			mTextView.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			ll.addView(mTextView);
		}

		return arg1;
	}

	public void setNotificationText(String notify) {
		mNotify = notify;
		sa.notifyDataSetChanged();
	}

}
