package mynuaa.whatever.DataSource;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import mynuaa.whatever.MyAlertDialog;
import mynuaa.whatever.MyAlertDialog.OnClickListener;
import mynuaa.whatever.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class UpdateTask extends Task {
	public static final int CHECK_SUCCESS = 0;
	public static final int CHECK_FAIL_INNER_ERROR = 1;

	private static final int PLATFORM = 0; // Android

	private final OnUpdateCheckListener mOnUpdateCheckListener;
	private final int mVersion;

	private int mResult = CHECK_SUCCESS;
	private VersionData mVersionData = null;

	public UpdateTask(String tag, Context context, OnUpdateCheckListener l) {
		super(tag);
		mOnUpdateCheckListener = l;
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (info == null) {
			mVersion = 0;
		} else {
			// 当前版本的版本号
			mVersion = info.versionCode;
		}
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("m", "c");
		params.put("p", String.valueOf(PLATFORM));
		params.put("v", String.valueOf(mVersion));

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_UPDATE, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = CHECK_FAIL_INNER_ERROR;
			return;
		}

		Log.d("https update", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode != 0) {
				mResult = CHECK_FAIL_INNER_ERROR;
				return;
			}

			String haveNewVersion = jsonObj.getString("n");

			if ("1".equals(haveNewVersion)) {// 有新版本
				String newVersion = jsonObj.getString("v");
				String versionName = jsonObj.getString("vn");
				String updateTime = jsonObj.getString("t");
				String versionDescription = jsonObj.getString("vd");
				String isCritical = jsonObj.getString("f");
				String fileSize = jsonObj.getString("s");

				long version = Long.parseLong(newVersion);
				Timestamp updateTime_t = Timestamp.valueOf(updateTime);
				boolean isCritical_b = "1".equals(isCritical);
				long fileSize_l = Long.parseLong(fileSize);

				mVersionData = new VersionData();
				mVersionData.version = version;
				mVersionData.versionName = versionName;
				mVersionData.updateTime = updateTime_t;
				mVersionData.versionDescription = versionDescription;
				mVersionData.isCritical = isCritical_b;
				mVersionData.fileSize = fileSize_l;
			}

			mResult = CHECK_SUCCESS;
		} catch (JSONException e) {
			e.printStackTrace();
			mResult = CHECK_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnUpdateCheckListener != null) {
			mOnUpdateCheckListener.onUpdateCheck(mResult, mVersionData);
		}
	}

	public static interface OnUpdateCheckListener {
		public void onUpdateCheck(int result, VersionData version);
	}

	public static class VersionData {
		public long version;
		public String versionName;
		public Timestamp updateTime;
		public String versionDescription;
		public boolean isCritical;
		public long fileSize;
	}

	@SuppressLint("DefaultLocale")
	public static void showUpdateDialog(final Context context,
			VersionData version) {
		String title = context.getString(R.string.update_dialog_title);
		String ver = context.getString(R.string.update_dialog_new_version,
				version.versionName);
		String size;
		float s = version.fileSize / 1024f;
		if (s < 500) {
			size = String.format("%.1fK", s);
		} else {
			s /= 1024;
			size = String.format("%.1fM", s);
		}
		size = context.getString(R.string.update_dialog_file_size, size);

		View dialogView = LayoutInflater.from(context).inflate(
				R.layout.dialog_update, null);

		((TextView) dialogView.findViewById(R.id.textView_update_version))
				.setText(Html.fromHtml(ver));
		((TextView) dialogView.findViewById(R.id.textView_update_des))
				.setText(version.versionDescription);
		((TextView) dialogView.findViewById(R.id.textViewupdate_size))
				.setText(size);

		Dialog alertDialog = new MyAlertDialog.Builder(context)
				.setTitle(title)
				.setView(dialogView)
				.setNegativeButton(R.string.button_cancel, null)
				.setPositiveButton(R.string.update_dialog_btn_update_now,
						new OnClickListener() {
							@Override
							public boolean onClick(DialogInterface dialog,
									int which) {
								String url = NetworkHelper.STR_SERVER_URL_UPDATE
										+ "?m=d&p=0";
								Intent i = new Intent(Intent.ACTION_VIEW);
								i.setData(Uri.parse(url));
								context.startActivity(i);
								return true;
							}
						}).create();
		alertDialog.show();
	}

	public static boolean hasUpdate(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		long lastUpdateCheckVersion = sp.getLong("updateV", -1);
		int version = -1;
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			version = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version < lastUpdateCheckVersion;
	}
}
