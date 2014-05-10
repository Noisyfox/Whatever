package mynuaa.whatever.SettingsWidget;

import java.util.ArrayList;
import java.util.List;

import mynuaa.whatever.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SettingsAdapter extends BaseAdapter {

	private List<Setting> mSettings = new ArrayList<Setting>();
	private Context mContext;
	protected SharedPreferences mPreferneces;

	public SettingsAdapter(Context context, String prefName) {
		mContext = context;
		mPreferneces = mContext.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);
	}

	public void addSetting(Setting s) {
		s.sa = this;
		mSettings.add(s);
		notifyDataSetChanged();
	}

	public void loadSettings() {
		for (Setting s : mSettings) {
			s.load(mPreferneces);
		}
	}

	public void onValueChanged() {
		Log.d("aaa", "Aasdasd");
		Editor e = mPreferneces.edit();
		for (Setting s : mSettings) {
			s.save(e);
		}
		e.commit();
	}

	@Override
	public int getCount() {
		return mSettings.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mSettings.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		Setting s = mSettings.get(arg0);

		if (arg1 == null)
			arg1 = LayoutInflater.from(mContext).inflate(R.layout.setting_item,
					arg2, false);

		((TextView) arg1.findViewById(R.id.textView_title)).setText(s.title);

		if (s.summary == null || s.summary.isEmpty()) {
			arg1.findViewById(R.id.textView_summary).setVisibility(View.GONE);
		} else {
			TextView tv = ((TextView) arg1.findViewById(R.id.textView_summary));
			tv.setVisibility(View.VISIBLE);
			tv.setText(s.summary);
		}

		return s.getView(mContext, arg1, arg2);
	}

}
