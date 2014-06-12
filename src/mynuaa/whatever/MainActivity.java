package mynuaa.whatever;

import java.util.LinkedList;
import java.util.List;

import mynuaa.whatever.DataSource.DataCenter;
import mynuaa.whatever.DataSource.NotificationCheckTask;
import mynuaa.whatever.DataSource.NotificationCheckTask.OnNotificationCheckListener;
import mynuaa.whatever.DataSource.NotificationData;
import mynuaa.whatever.DataSource.UpdateTask;
import mynuaa.whatever.DataSource.UpdateTask.OnUpdateCheckListener;
import mynuaa.whatever.DataSource.UpdateTask.VersionData;
import mynuaa.whatever.DataSource.UserInfoSyncTask.OnUserInfoSyncListener;
import mynuaa.whatever.DataSource.UserInfoSyncTask;
import mynuaa.whatever.DataSource.UserSession;
import mynuaa.whatever.DataSource.UserSession.UserInfo;
import mynuaa.whatever.DrawerAdapter.DrawerItem;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ActionProvider;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentActivity implements
		OnUserInfoSyncListener, OnNotificationCheckListener,
		OnUpdateCheckListener {
	private static final String TASK_TAG = "task_main_activity";
	private static long exitFirstTime = 0;

	protected DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	private DrawerAdapter mDrawerAdapter = null;

	private int mCurrentFragment = -1;

	private MainFragment mMainFragment = new MainFragment();

	@Override
	public void finish() {
		if (mCurrentFragment != R.id.drawer_mainpage) {// 如果当前选项卡不在主页，则先返回主页
			selectItem(1);
		} else {
			long secondTime = System.currentTimeMillis();
			if (secondTime - exitFirstTime > 800) {// 如果两次按键时间间隔大于800毫秒，则不退出
				Toast.makeText(MainActivity.this, "连续点击退出", Toast.LENGTH_SHORT)
						.show();
				exitFirstTime = secondTime;
			} else {
				super.finish();
			}
		}
	}

	public void forceFinish() {
		super.finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getBooleanExtra("notification", false)) {
			selectItem(2);
		}

		Log.d("aaa", "onNewIntent");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerAdapter = new DrawerAdapter(this, R.xml.drawer);
		mDrawerList.setAdapter(mDrawerAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu(); // creates call to
												// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu(); // creates call to
												// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mMainFragment.mMessageGroupIsContact = sp.getBoolean("mainLastGroup",
				false);

		if (NotificationData.getUnreadNotification().size() > 0) {
			mDrawerAdapter.setNotification(R.id.drawer_message, true);
		}

		if (UpdateTask.hasUpdate(this)) {
			mDrawerAdapter.setNotification(R.id.drawer_settings, true);
		}

		boolean needChkNotiNow = NotificationCheckTask.checkEnabled(this);
		if (getIntent().getBooleanExtra("notification", false)) {
			// needChkNotiNow = false;
			selectItem(2);
		} else if (savedInstanceState == null) {
			selectItem(1);
		}

		updateUserInfo();

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
		WhateverApplication.getApplication()
				.registerOnNotificationCheckListener(this);
		AlarmReceiver.startAlarm(this);

		DataCenter.startLoactionService();

		userInfoSync(UserInfoSyncTask.LOCAL);
		if (needChkNotiNow) {
			WhateverApplication.getApplication().checkNoitifcation();
		}
		WhateverApplication.getMainTaskManager().startTask(
				new UpdateTask("Global", this, this));
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
		WhateverApplication.getApplication()
				.registerOnNotificationCheckListener(this);
		AlarmReceiver.startAlarm(this);
	}

	@Override
	protected void onDestroy() {
		AlarmReceiver.stopAlarm(this);
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);
		WhateverApplication.getApplication()
				.unregisterOnNotificationCheckListener(this);
		DataCenter.stopLocationService();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor e = sp.edit();
		e.putBoolean("mainLastGroup", mMainFragment.mMessageGroupIsContact);
		e.commit();
		super.onDestroy();
	}

	public void userInfoSync(int type) {
		WhateverApplication.getMainTaskManager().startTask(
				new UserInfoSyncTask(TASK_TAG, UserSession.getCurrentSession(),
						type, this));
	}

	public void userInfoSync(String realName, String phone) {
		WhateverApplication.getMainTaskManager().startTask(
				new UserInfoSyncTask(TASK_TAG, UserSession.getCurrentSession(),
						realName, phone, this));
	}

	private void updateUserInfo() {
		UserSession us = UserSession.getCurrentSession();
		if (us == null) {
			return;
		}

		UserInfo ui = us.getUserInfo();
		String name = ui.getUid();
		Bitmap head = ui.getHeadBitmap();

		mDrawerAdapter.setTitle(R.id.drawer_user, name + " ");

		if (head != null) {
			mDrawerAdapter.setIcon(R.id.drawer_user, head);
		} else {
			mDrawerAdapter.setIcon(R.id.drawer_user, BitmapFactory
					.decodeResource(getResources(), R.drawable.head_none));
		}
	}

	private List<OnUserInfoSyncListener> mOnUserInfoSyncListeners = new LinkedList<OnUserInfoSyncListener>();

	public void registerOnUserInfoSyncListener(OnUserInfoSyncListener listener) {
		synchronized (mOnUserInfoSyncListeners) {
			if (!mOnUserInfoSyncListeners.contains(listener)) {
				mOnUserInfoSyncListeners.add(listener);
			}
		}
	}

	public void unregisterOnUserInfoSyncListener(OnUserInfoSyncListener listener) {
		synchronized (mOnUserInfoSyncListeners) {
			mOnUserInfoSyncListeners.remove(listener);
		}
	}

	@Override
	public void onSyncFinished(int result, int syncType, UserSession session) {
		if (result == UserInfoSyncTask.SYNC_SUCCESS) {
			session.saveAsLocalSession(this);
		}
		updateUserInfo();
		synchronized (mOnUserInfoSyncListeners) {
			for (OnUserInfoSyncListener ouisl : mOnUserInfoSyncListeners) {
				ouisl.onSyncFinished(result, syncType, session);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(getMenuItem(item))) {
			return true;
		}

		// Handle action buttons
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private android.view.MenuItem getMenuItem(final MenuItem item) {
		return new android.view.MenuItem() {
			@Override
			public int getItemId() {
				return item.getItemId();
			}

			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean collapseActionView() {

				return false;
			}

			@Override
			public boolean expandActionView() {

				return false;
			}

			@Override
			public ActionProvider getActionProvider() {

				return null;
			}

			@Override
			public View getActionView() {

				return null;
			}

			@Override
			public char getAlphabeticShortcut() {

				return 0;
			}

			@Override
			public int getGroupId() {

				return 0;
			}

			@Override
			public Drawable getIcon() {

				return null;
			}

			@Override
			public Intent getIntent() {

				return null;
			}

			@Override
			public ContextMenuInfo getMenuInfo() {

				return null;
			}

			@Override
			public char getNumericShortcut() {

				return 0;
			}

			@Override
			public int getOrder() {

				return 0;
			}

			@Override
			public SubMenu getSubMenu() {

				return null;
			}

			@Override
			public CharSequence getTitle() {

				return null;
			}

			@Override
			public CharSequence getTitleCondensed() {

				return null;
			}

			@Override
			public boolean hasSubMenu() {

				return false;
			}

			@Override
			public boolean isActionViewExpanded() {

				return false;
			}

			@Override
			public boolean isCheckable() {

				return false;
			}

			@Override
			public boolean isChecked() {

				return false;
			}

			@Override
			public boolean isVisible() {

				return false;
			}

			@Override
			public android.view.MenuItem setActionProvider(
					ActionProvider actionProvider) {

				return null;
			}

			@Override
			public android.view.MenuItem setActionView(View view) {

				return null;
			}

			@Override
			public android.view.MenuItem setActionView(int resId) {

				return null;
			}

			@Override
			public android.view.MenuItem setAlphabeticShortcut(char alphaChar) {

				return null;
			}

			@Override
			public android.view.MenuItem setCheckable(boolean checkable) {

				return null;
			}

			@Override
			public android.view.MenuItem setChecked(boolean checked) {

				return null;
			}

			@Override
			public android.view.MenuItem setEnabled(boolean enabled) {

				return null;
			}

			@Override
			public android.view.MenuItem setIcon(Drawable icon) {

				return null;
			}

			@Override
			public android.view.MenuItem setIcon(int iconRes) {

				return null;
			}

			@Override
			public android.view.MenuItem setIntent(Intent intent) {

				return null;
			}

			@Override
			public android.view.MenuItem setNumericShortcut(char numericChar) {

				return null;
			}

			@Override
			public android.view.MenuItem setOnActionExpandListener(
					OnActionExpandListener listener) {

				return null;
			}

			@Override
			public android.view.MenuItem setOnMenuItemClickListener(
					OnMenuItemClickListener menuItemClickListener) {

				return null;
			}

			@Override
			public android.view.MenuItem setShortcut(char numericChar,
					char alphaChar) {

				return null;
			}

			@Override
			public void setShowAsAction(int actionEnum) {

			}

			@Override
			public android.view.MenuItem setShowAsActionFlags(int actionEnum) {

				return null;
			}

			@Override
			public android.view.MenuItem setTitle(CharSequence title) {

				return null;
			}

			@Override
			public android.view.MenuItem setTitle(int title) {

				return null;
			}

			@Override
			public android.view.MenuItem setTitleCondensed(CharSequence title) {

				return null;
			}

			@Override
			public android.view.MenuItem setVisible(boolean visible) {

				return null;
			}
		};
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	public void jumpToSettings() {
		selectItem(4);
	}

	private void selectItem(int position) {
		/*
		 * // update the main content by replacing fragments PlanetFragment
		 * fragment = new PlanetFragment(); Bundle args = new Bundle();
		 * args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		 * fragment.setArguments(args);
		 * 
		 * FragmentManager fragmentManager = getSupportFragmentManager();
		 * fragmentManager.beginTransaction().replace(R.id.content_frame,
		 * fragment).commit();
		 * 
		 * // update selected item and title, then close the drawer
		 * mDrawerList.setItemChecked(position, true);
		 * setTitle(mPlanetTitles[position]);
		 * mDrawerLayout.closeDrawer(mDrawerList);
		 */

		DrawerItem di = (DrawerItem) mDrawerAdapter.getItem(position);

		int id = di.id;

		if (mCurrentFragment == id) {
			mDrawerLayout.closeDrawer(mDrawerList);

			if (mCurrentFragment == R.id.drawer_mainpage) {
				mMainFragment.returnTop();
			}
			return;
		}

		Fragment fragment;
		String title = di.title;

		switch ((int) id) {
		case R.id.drawer_user:
			fragment = new UserFragment();
			// title = "";
			break;
		case R.id.drawer_mainpage:
			fragment = mMainFragment;
			title = "Whatever";
			break;
		case R.id.drawer_message:
			fragment = new NotifyFragment();
			// title = "消息";
			break;
		case R.id.drawer_path:
			fragment = new PathFragment();
			// title = "";
			break;
		case R.id.drawer_settings:
			fragment = new SettingsFragment();
			// title = "";
			break;
		default:
			mDrawerLayout.closeDrawer(mDrawerList);
			return;
		}

		// update the main content by replacing fragments

		// args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		// fragment.setArguments(args);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// ft.setCustomAnimations(R.anim.slide_in_bottom,
		// R.anim.slide_out_top);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.replace(R.id.content_frame, fragment);
		ft.commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		// setTitle(mPlanetTitles[position]);
		setTitle(title);
		mDrawerLayout.closeDrawer(mDrawerList);
		mCurrentFragment = id;
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onNotificationCheck(int result, int unreadCount) {
		UserSession session = UserSession.getCurrentSession();
		if (result == NotificationCheckTask.CHECK_SUCCESS && unreadCount != 0
				&& unreadCount > session.getUnreadNotificationCount()) {

			session.setUnreadNotification(unreadCount);
			session.saveAsLocalSession(this);

			// 消息通知栏
			// 定义NotificationManager
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			// 定义通知栏展现的内容信息
			int icon = R.drawable.ic_launcher;
			CharSequence tickerText = "你收到" + unreadCount + "条新消息，点击查看";
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);

			// 定义下拉通知栏时要展现的内容信息
			Context context = getApplicationContext();
			CharSequence contentTitle = "你收到" + unreadCount + "条新消息";
			CharSequence contentText = "点击查看";
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClass(this, StartupActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.putExtra("notification", true);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
			mNotificationManager.notify(1, notification);
		}

		if (session.getUnreadNotificationCount() > 0) {
			mDrawerAdapter.setNotification(R.id.drawer_message, true);
		}
	}

	@SuppressWarnings("deprecation")
	private void testNoty() {
		// 消息通知栏
		// 定义NotificationManager
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		// 定义通知栏展现的内容信息
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "你收到" + 11111 + "条新消息，点击查看";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);

		// 定义下拉通知栏时要展现的内容信息
		Context context = getApplicationContext();
		CharSequence contentTitle = "你收到" + 1111 + "条新消息";
		CharSequence contentText = "点击查看";

		Intent intent = new Intent(Intent.ACTION_MAIN);

		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		intent.setClass(this, StartupActivity.class);

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		intent.putExtra("notification", true);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
		mNotificationManager.notify(1, notification);
	}

	public void clearNotification() {
		UserSession session = UserSession.getCurrentSession();
		if (session.getUnreadNotificationCount() == 0
				&& NotificationData.getUnreadNotification().size() == 0) {
			mDrawerAdapter.setNotification(R.id.drawer_message, false);
		}
	}

	@Override
	public void onUpdateCheck(int result, VersionData version) {
		if (result == UpdateTask.CHECK_SUCCESS && version != null) {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);

			long lastUpdateCheckVersion = sp.getLong("updateV", -1);
			if (version.version > lastUpdateCheckVersion) {
				UpdateTask.showUpdateDialog(this, version);
			}

			Editor e = sp.edit();
			e.putLong("updateV", version.version);
			e.commit();
		}
	}

}
