package mynuaa.whatever.SettingsWidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;

public abstract class Setting {
	public String title = "";
	public String id = "";
	public String summary = "";

	protected SettingsAdapter sa = null;

	public Setting(String title, String id) {
		this.title = title;
		this.id = id;
	}

	public abstract void load(SharedPreferences sp);

	public abstract void save(Editor e);

	public abstract void OnClick();

	public abstract View getView(Context context, View arg1, ViewGroup arg2);

	public void onValueChanged() {
		sa.onValueChanged();
	}

	public void setTitle(String title) {
		this.title = title;
		if (sa != null)
			sa.notifyDataSetChanged();
	}

	public void setSummary(String summary) {
		this.summary = summary;
		if (sa != null)
			sa.notifyDataSetChanged();
	}

	public SharedPreferences getSharedPreferences() {
		return sa.mPreferneces;
	}
}
