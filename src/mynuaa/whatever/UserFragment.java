package mynuaa.whatever;

import mynuaa.whatever.DataSource.UserInfoSyncTask;
import mynuaa.whatever.DataSource.UserInfoSyncTask.OnUserInfoSyncListener;
import mynuaa.whatever.DataSource.UserSession;
import mynuaa.whatever.DataSource.UserSession.UserInfo;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class UserFragment extends SherlockFragment implements
		OnUserInfoSyncListener {
	// private static final String TASK_TAG = "task_user_fragment";

	private TextView mTextView_userid;
	private EditText mEditText_name, mEditText_phone;
	private ImageView mImageView_background, mImageView_head;

	private ProgressDialog mUpdatingDialog = null;

	private boolean mEditIconIsVisible = true;
	private boolean mEditIconIsEdit = false;
	private Menu mMenu = null;

	// private UserSession mSession;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_user, container,
				false);

		mTextView_userid = (TextView) rootView
				.findViewById(R.id.textView_userid);
		mEditText_name = (EditText) rootView.findViewById(R.id.textView_name);
		mEditText_phone = (EditText) rootView.findViewById(R.id.textView_phone);
		mImageView_background = (ImageView) rootView
				.findViewById(R.id.imageView_background);
		mImageView_head = (ImageView) rootView
				.findViewById(R.id.imageView_head);

		updateInfo(UserSession.getCurrentSession());
		// mSession = UserSession.getCurrentSession();

		// WhateverApplication.getMainTaskManager().startTask(
		// new UserInfoSyncTask(TASK_TAG, mSession,
		// UserInfoSyncTask.DOWNLOAD, this));

		Util.setEditable(mEditText_name, false);
		Util.setEditable(mEditText_phone, false);

		setHasOptionsMenu(true);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mMenu = menu;
		inflater.inflate(R.menu.userpage, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		FragmentActivity a = this.getActivity();

		if (MainActivity.class.isInstance(a)) {
			MainActivity ma = (MainActivity) a;
			boolean drawerOpen = ma.mDrawerLayout.isDrawerOpen(ma.mDrawerList);

			mEditIconIsVisible = !drawerOpen;
			updateIcon(menu);
		}
	}

	private void updateIcon(Menu menu) {
		if (!mEditIconIsVisible) {
			menu.findItem(R.id.edit_user_info).setVisible(false);
			menu.findItem(R.id.save_user_info).setVisible(false);
			menu.findItem(R.id.cancel_user_info).setVisible(false);
		} else {
			menu.findItem(R.id.edit_user_info).setVisible(!mEditIconIsEdit);
			menu.findItem(R.id.save_user_info).setVisible(mEditIconIsEdit);
			menu.findItem(R.id.cancel_user_info).setVisible(mEditIconIsEdit);
		}
		mEditText_name.setError(null);
		mEditText_phone.setError(null);
		Util.setEditable(mEditText_name, mEditIconIsEdit);
		Util.setEditable(mEditText_phone, mEditIconIsEdit);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.edit_user_info: {
			mEditIconIsEdit = true;
			updateIcon(mMenu);

			mEditText_name.requestFocus();
			Util.setInputMethod(getActivity(), true);
			mEditText_name.selectAll();
			return true;
		}
		case R.id.save_user_info: {
			/*
			 * mEditIconIsEdit = false; updateIcon(mMenu);
			 * Util.setInputMethod(getActivity(), false);
			 */
			attemptUpdate();
			return true;
		}
		case R.id.cancel_user_info: {
			mEditIconIsEdit = false;
			updateIcon(mMenu);
			updateInfo(UserSession.getCurrentSession());
			Util.setInputMethod(getActivity(), false);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean mUpdating = false;

	private void attemptUpdate() {
		if (mUpdating) {
			return;
		}

		Util.setInputMethod(getActivity(), false);
		// Reset errors.
		mEditText_name.setError(null);
		mEditText_phone.setError(null);

		String real_name = mEditText_name.getText().toString(), phone = mEditText_phone
				.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(real_name)) {
			mEditText_name.setError(getString(R.string.error_field_required));
			focusView = mEditText_name;
			cancel = true;
		}

		// Check for a valid password.
		if (TextUtils.isEmpty(phone)) {
			mEditText_phone.setError(getString(R.string.error_field_required));
			focusView = mEditText_phone;
			cancel = true;
		} else if (phone.length() != 11) {
			mEditText_phone.setError(getString(R.string.error_invalid_phone));
			focusView = mEditText_phone;
			cancel = true;
		}
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {

			Activity activity = getActivity();
			if (MainActivity.class.isInstance(activity)) {
				mUpdatingDialog = ProgressDialog.show(getActivity(), "保存中",
						"请稍候", false);
				((MainActivity) activity).userInfoSync(real_name, phone);
			} else {

			}
		}
	}

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
		if (MainActivity.class.isInstance(activity)) {
			((MainActivity) activity).registerOnUserInfoSyncListener(this);
			((MainActivity) activity).userInfoSync(UserInfoSyncTask.DOWNLOAD);
		}
	}

	@Override
	public void onDetach() {
		// WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);
		Activity activity = getActivity();
		if (MainActivity.class.isInstance(activity)) {
			((MainActivity) activity).registerOnUserInfoSyncListener(this);
		}
		super.onDetach();
	}

	private void updateInfo(UserSession session) {
		UserInfo ui = session.getUserInfo();
		mTextView_userid.setText(ui.getUid());
		mEditText_name.setText(ui.getName());
		mEditText_phone.setText(ui.getPhone());
		mImageView_head.setImageBitmap(ui.getHeadBitmap());
		mImageView_background.setImageBitmap(ui.getHeadBlurBitmap());
	}

	@Override
	public void onSyncFinished(int result, int syncType, UserSession session) {
		if (!mEditIconIsEdit)
			updateInfo(session);

		if (syncType == UserInfoSyncTask.UPLOAD) {
			mEditIconIsEdit = false;
			updateIcon(mMenu);
			if (mUpdatingDialog != null) {
				mUpdatingDialog.dismiss();
				mUpdatingDialog = null;
			}

			Activity activity = getActivity();
			if (activity != null) {
				if (result == UserInfoSyncTask.SYNC_SUCCESS) {
					Toast.makeText(activity, "更新成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(activity, "更新失败", Toast.LENGTH_SHORT).show();
				}
			}
		}

	}
}
