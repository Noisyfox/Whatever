package mynuaa.whatever.SettingsWidget;

import mynuaa.whatever.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class TextSetting extends Setting {
	private TextView mTextView;

	private CharSequence mText = null;
	private int mResid = -1;

	public TextSetting(String title, String id) {
		super(title, id);
	}

	@Override
	public void load(SharedPreferences sp) {
	}

	@Override
	public void save(Editor e) {
	}

	@Override
	public View getView(Context context, View arg1, ViewGroup arg2) {
		LinearLayout ll = (LinearLayout) arg1
				.findViewById(R.id.content_container);

		ll.removeAllViews();

		mTextView = new TextView(context);
		if (mText != null) {
			mTextView.setText(mText);
		} else {
			mTextView.setText(mResid);
		}
		mTextView.setTextColor(0xff3581C2);
		mTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));
		ll.addView(mTextView);

		return arg1;
	}

	public void setText(CharSequence text) {
		mText = text;
		mResid = -1;
		if (mTextView != null) {
			mTextView.setText(mText);
		}
	}

	public void setText(int resid) {
		mText = null;
		mResid = resid;
		if (mTextView != null) {
			mTextView.setText(resid);
		}
	}

}
