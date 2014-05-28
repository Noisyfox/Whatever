package mynuaa.whatever;

import mynuaa.whatever.DataSource.CacheClearTask;
import mynuaa.whatever.DataSource.CacheClearTask.OnCacheClearListener;
import mynuaa.whatever.DataSource.UserSession;
import mynuaa.whatever.SettingsWidget.ButtonSetting;
import mynuaa.whatever.SettingsWidget.Setting;
import mynuaa.whatever.SettingsWidget.SettingsAdapter;
import mynuaa.whatever.SettingsWidget.ToggleSetting;
import mynuaa.whatever.SettingsWidget.ToggleSetting.OnToggleSettingChangeListener;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragment;

public class SettingsFragment extends SherlockFragment implements
		OnItemClickListener, OnCacheClearListener {

	private static final String TASK_TAG = "task_settings_fragment";
	private ProgressDialog mCacheClearDialog = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings, container,
				false);

		SettingsAdapter sa = new SettingsAdapter(getActivity(), "Settings");
		setupSettings(sa);
		sa.loadSettings();

		ListView settingsView = (ListView) rootView
				.findViewById(R.id.listView_settings);
		settingsView.setAdapter(sa);
		settingsView.setOnItemClickListener(this);

		rootView.findViewById(R.id.button_exit).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Dialog alertDialog = new MyAlertDialog.Builder(
								getActivity())
								.setMessage("确定要退出当前账号么？")
								.setPositiveButton("退出",
										new MyAlertDialog.OnClickListener() {
											@Override
											public boolean onClick(
													DialogInterface dialog,
													int which) {
												UserSession
														.clearLocalSession(getActivity());
												CacheClearTask.clearCache();

												Intent i = new Intent();
												i.setClass(getActivity(),
														StartupActivity.class);
												i.putExtra("skipSplash", true);
												getActivity().startActivity(i);

												((MainActivity) getActivity())
														.forceFinish();

												return true;
											}
										}).setNegativeButton("取消", null)
								.create();
						alertDialog.show();
					}
				});

		return rootView;
	}

	private static final String SP_READ_CONTACT_NOTIFY = "readContactNotified";

	private void setupSettings(SettingsAdapter settingsAdapter) {
		settingsAdapter.addSetting(new ToggleSetting("匹配手机通讯录", "autoContact",
				new OnToggleSettingChangeListener() {
					@Override
					public boolean onValueChange(ToggleSetting ts,
							boolean newValue) {
						if (newValue) {
							final SharedPreferences sp = ts
									.getSharedPreferences();
							boolean no = sp.getBoolean(SP_READ_CONTACT_NOTIFY,
									false);

							if (!no) {
								Dialog alertDialog = new MyAlertDialog.Builder(
										getActivity())
										.setMessage(
												"\u3000\u3000开启通讯录匹配将会上传您的通讯录信息到服务器以显示通讯录好友发布的状态。是否同意？")
										.setPositiveButton(
												"同意",
												new MyAlertDialog.OnClickListener() {
													@Override
													public boolean onClick(
															DialogInterface dialog,
															int which) {
														Editor e = sp.edit();
														e.putBoolean(
																SP_READ_CONTACT_NOTIFY,
																true);
														e.commit();

														return true;
													}
												})
										.setNegativeButton("取消", null).create();
								alertDialog.show();

								return false;
							}
						}
						return true;
					}
				}));
		settingsAdapter.addSetting(new ToggleSetting("新消息通知", "messageNoti",
				null));
		settingsAdapter.addSetting(new ButtonSetting("清除缓存", "clearCache") {
			@Override
			public void OnClick() {
				Dialog alertDialog = new MyAlertDialog.Builder(getActivity())
						.setMessage("清空缓存的图片、状态等信息？")
						.setPositiveButton("清除缓存",
								new MyAlertDialog.OnClickListener() {
									@Override
									public boolean onClick(
											DialogInterface dialog, int which) {
										mCacheClearDialog = ProgressDialog
												.show(getActivity(), "清除缓存",
														"请稍候", false);
										WhateverApplication
												.getMainTaskManager()
												.startTask(
														new CacheClearTask(
																TASK_TAG,
																SettingsFragment.this));

										return true;
									}
								}).setNegativeButton("取消", null).create();
				alertDialog.show();
			}
		});
		settingsAdapter.addSetting(new ButtonSetting("关于 Whatever", "about") {
			@Override
			public void OnClick() {
				Intent i = new Intent();
				i.setClass(getActivity(), AboutActivity.class);
				getActivity().startActivity(i);
				getActivity().overridePendingTransition(R.anim.slide_in_right,
						R.anim.stay);
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
	}

	@Override
	public void onDetach() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);

		super.onDetach();
	}

	@Override
	public void onCacheClear() {
		mCacheClearDialog.dismiss();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Setting s = (Setting) arg0.getAdapter().getItem(arg2);
		s.OnClick();
	}
}
