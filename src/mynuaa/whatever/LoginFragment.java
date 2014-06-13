package mynuaa.whatever;

import mynuaa.whatever.DataSource.ContactSyncTask;
import mynuaa.whatever.DataSource.LoginTask;
import mynuaa.whatever.DataSource.TaskManager;
import mynuaa.whatever.DataSource.LoginTask.OnLoginListener;
import mynuaa.whatever.DataSource.UserSession;

import com.actionbarsherlock.app.SherlockFragment;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginFragment extends SherlockFragment implements OnKeyListener,
		OnLoginListener {
	private static final String TASK_TAG = "task_login_fragment";

	private boolean mAuthing = false;

	// Values for email and password at the time of the login attempt.
	private String mUID;
	private String mPassword;

	// UI references.
	private EditText mUIDView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (StartupActivity.class.isInstance(activity)) {
			((StartupActivity) activity).setOnKeyUpListener(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout wrapper = new LinearLayout(getActivity());
		View rootView = inflater
				.inflate(R.layout.fragment_login, wrapper, true);

		// Set up the login form.
		mUIDView = (EditText) rootView.findViewById(R.id.login_id);

		mPasswordView = (EditText) rootView.findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = rootView.findViewById(R.id.login_form);
		mLoginStatusView = rootView.findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) rootView
				.findViewById(R.id.login_status_message);

		rootView.findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

		return wrapper;
	}

	boolean autoLogin = false;

	@Override
	public void onResume() {
		super.onResume();
		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		if (!autoLogin) {
			autoLogin = true;
			UserSession session = UserSession.loadLocalSession(getActivity());
			if (session != null) {
				mUIDView.setText(session.getUserInfo().getUid());
				// 尝试登陆
				preLogin(session);
			}
		}
	}

	@Override
	public void onDetach() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);
		super.onDetach();
	}

	@Override
	public void onLoginFinished(int result, UserSession session) {
		switch (result) {
		case LoginTask.LOGIN_SUCCESS: {
			session.saveAsLocalSession(getActivity());

			if (ContactSyncTask.contactEnabled(getActivity())
					&& !ContactSyncTask.checkContactChange(getActivity())) {
				WhateverApplication.getMainTaskManager().startTask(
						new ContactSyncTask(TaskManager.TAG_GLOBAL,
								getActivity(), null));
			}

			Intent i = new Intent();
			i.setClass(getActivity(), MainActivity.class);
			i.putExtra("notification", getActivity().getIntent()
					.getBooleanExtra("notification", false));
			startActivity(i);
			getActivity().finish();
			break;
		}
		case LoginTask.LOGIN_FAIL_WRONG: {
			mPasswordView
					.setError(getString(R.string.error_incorrect_password));
			mPasswordView.requestFocus();
			showProgress(false);
			break;
		}
		case LoginTask.LOGIN_FAIL_SESSION_TIME_OUT: {
			Toast.makeText(getActivity(), "登陆信息已过期，请重新登陆", Toast.LENGTH_LONG)
					.show();
			showProgress(false);
			break;
		}
		case LoginTask.LOGIN_FAIL_INNER_ERROR:
		default:
			Toast.makeText(getActivity(), "内部错误，请稍后重试", Toast.LENGTH_LONG)
					.show();
			showProgress(false);
		}
		mAuthing = false;
	}

	private void preLogin(UserSession session) {
		if (mAuthing) {
			return;
		}

		mAuthing = true;
		Util.setInputMethod(getActivity(), false);

		mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
		showProgress(true);

		WhateverApplication.getMainTaskManager().startTask(
				new LoginTask(TASK_TAG, session, this));
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthing) {
			return;
		}
		Util.setInputMethod(getActivity(), false);

		// Reset errors.
		mUIDView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUID = mUIDView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mUID)) {
			mUIDView.setError(getString(R.string.error_field_required));
			focusView = mUIDView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthing = true;
			WhateverApplication.getMainTaskManager().startTask(
					new LoginTask(TASK_TAG, mUID, mPassword, this));
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
		if (mAuthing && arg1 == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return false;
	}
}
