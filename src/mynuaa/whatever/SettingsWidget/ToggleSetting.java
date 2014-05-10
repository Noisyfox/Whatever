package mynuaa.whatever.SettingsWidget;

import mynuaa.whatever.R;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ToggleSetting extends Setting {
	OnToggleSettingChangeListener mChangeListener;
	boolean checked = false;
	Switch mSwitch;

	public ToggleSetting(String title, String id,
			OnToggleSettingChangeListener onToggleSettingChangeListener) {
		super(title, id);
		mChangeListener = onToggleSettingChangeListener;
	}

	@Override
	public void OnClick() {
		mSwitch.setChecked(!checked);
	}

	@Override
	public View getView(Context context, View arg1, ViewGroup arg2) {
		LinearLayout ll = (LinearLayout) arg1
				.findViewById(R.id.content_container);

		ll.removeAllViews();

		mSwitch = new Switch(context);

		mSwitch.setChecked(checked);

		mSwitch.setFocusable(false);

		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (checked != arg1) {
					if (mChangeListener != null
							&& !mChangeListener.onValueChange(
									ToggleSetting.this, arg1)) {
						mSwitch.setChecked(checked);
					} else {
						checked = arg1;
						onValueChanged();
					}
				}
			}
		});

		ll.addView(mSwitch);

		return arg1;
	}

	@Override
	public void load(SharedPreferences sp) {
		checked = sp.getBoolean(id, checked);
	}

	@Override
	public void save(Editor e) {
		e.putBoolean(id, checked);
	}

	public static interface OnToggleSettingChangeListener {
		public boolean onValueChange(ToggleSetting ts, boolean newValue);
	}
}
