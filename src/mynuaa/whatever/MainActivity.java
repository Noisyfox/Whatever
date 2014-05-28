package mynuaa.whatever;

import java.util.LinkedList;
import java.util.List;

import mynuaa.whatever.DataSource.DataCenter;
import mynuaa.whatever.DataSource.NotificationCheckTask;
import mynuaa.whatever.DataSource.NotificationCheckTask.OnNotificationCheckListener;
import mynuaa.whatever.DataSource.NotificationData;
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
import android.view.ActionProvider;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentActivity implements
		OnUserInfoSyncListener, OnNotificationCheckListener {
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
		if (mCurrentFragment != R.id.drawer_mainpage) {// �����ǰѡ�������ҳ�����ȷ�����ҳ
			selectItem(1);
		} else {
			long secondTime = System.currentTimeMillis();
			if (secondTime - exitFirstTime > 800) {// ������ΰ���ʱ��������800���룬���˳�
				Toast.makeText(MainActivity.this, "��������˳�", Toast.LENGTH_SHORT)
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

		boolean needChkNotiNow = true;
		if (getIntent().getBooleanExtra("notification", false)) {
			needChkNotiNow = false;
			selectItem(2);
		} else if (savedInstanceState == null) {
			selectItem(1);
		}

		updateUserInfo();

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
		WhateverApplication.getApplication()
				.registerOnNotificationCheckListener(this);

		DataCenter.startLoactionService();

		userInfoSync(UserInfoSyncTask.LOCAL);
		if (needChkNotiNow) {
			WhateverApplication.getApplication().checkNoitifcation();
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
		WhateverApplication.getApplication()
				.registerOnNotificationCheckListener(this);
	}

	@Override
	protected void onDestroy() {
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
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean expandActionView() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public ActionProvider getActionProvider() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public View getActionView() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getAlphabeticShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getGroupId() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Drawable getIcon() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Intent getIntent() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ContextMenuInfo getMenuInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getNumericShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getOrder() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public SubMenu getSubMenu() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitle() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitleCondensed() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean hasSubMenu() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isActionViewExpanded() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCheckable() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isChecked() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isVisible() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public android.view.MenuItem setActionProvider(
					ActionProvider actionProvider) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(View view) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(int resId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setAlphabeticShortcut(char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setCheckable(boolean checkable) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setChecked(boolean checked) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setEnabled(boolean enabled) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(Drawable icon) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(int iconRes) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIntent(Intent intent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setNumericShortcut(char numericChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnActionExpandListener(
					OnActionExpandListener listener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnMenuItemClickListener(
					OnMenuItemClickListener menuItemClickListener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setShortcut(char numericChar,
					char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setShowAsAction(int actionEnum) {
				// TODO Auto-generated method stub

			}

			@Override
			public android.view.MenuItem setShowAsActionFlags(int actionEnum) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(int title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitleCondensed(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setVisible(boolean visible) {
				// TODO Auto-generated method stub
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
			// title = "��Ϣ";
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
		if (result == NotificationCheckTask.CHECK_SUCCESS && unreadCount != 0) {
			session.addUnreadNotification(unreadCount);
			session.saveAsLocalSession(this);

			// ��Ϣ֪ͨ��
			// ����NotificationManager
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			// ����֪ͨ��չ�ֵ�������Ϣ
			int icon = R.drawable.ic_launcher;
			CharSequence tickerText = "���յ�" + unreadCount + "������Ϣ������鿴";
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);

			// ��������֪ͨ��ʱҪչ�ֵ�������Ϣ
			Context context = getApplicationContext();
			CharSequence contentTitle = "���յ�" + unreadCount + "������Ϣ";
			CharSequence contentText = "����鿴";
			Intent notificationIntent = new Intent(this, MainActivity.class);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			notificationIntent.putExtra("notification", true);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;

			// ��mNotificationManager��notify����֪ͨ�û����ɱ�������Ϣ֪ͨ
			mNotificationManager.notify(1, notification);
		}

		if (session.getUnreadNotificationCount() > 0) {
			mDrawerAdapter.setNotification(R.id.drawer_message, true);
		}
	}

	public void clearNotification() {
		UserSession session = UserSession.getCurrentSession();
		if (session.getUnreadNotificationCount() == 0
				&& NotificationData.getUnreadNotification().size() == 0) {
			mDrawerAdapter.setNotification(R.id.drawer_message, false);
		}
	}

}
