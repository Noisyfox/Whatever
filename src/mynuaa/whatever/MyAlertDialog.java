package mynuaa.whatever;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAlertDialog {

	public static class Builder {
		private final Context mContext;
		private CharSequence mTitle = null;
		private CharSequence mMessage = null;
		private Drawable mIcon = null;
		private int mIconId = -1;
		private View mView = null;
		private CharSequence mPositiveButtonText = null;
		private OnClickListener mPositiveButtonListener = null;
		private CharSequence mNegativeButtonText = null;
		private OnClickListener mNegativeButtonListener = null;
		private boolean mCancelable = true;

		public Builder(Context context) {
			mContext = context;
		}

		public Builder setTitle(CharSequence title) {
			mTitle = title;
			return this;
		}

		public Builder setTitle(int titleId) {
			mTitle = mContext.getText(titleId);
			return this;
		}

		public Builder setMessage(int messageId) {
			mMessage = mContext.getText(messageId);
			return this;
		}

		public Builder setMessage(CharSequence message) {
			mMessage = message;
			return this;
		}

		public Builder setIcon(int iconId) {
			mIconId = iconId;
			mIcon = null;
			return this;
		}

		public Builder setIcon(Drawable icon) {
			mIcon = icon;
			mIconId = -1;
			return this;
		}

		public Builder setView(View view) {
			mView = view;
			return this;
		}

		public Builder setPositiveButton(int textId,
				final OnClickListener listener) {
			mPositiveButtonText = mContext.getText(textId);
			mPositiveButtonListener = listener;
			return this;
		}

		public Builder setPositiveButton(CharSequence text,
				final OnClickListener listener) {
			mPositiveButtonText = text;
			mPositiveButtonListener = listener;
			return this;
		}

		public Builder setNegativeButton(int textId,
				final OnClickListener listener) {
			mNegativeButtonText = mContext.getText(textId);
			mNegativeButtonListener = listener;
			return this;
		}

		public Builder setNegativeButton(CharSequence text,
				final OnClickListener listener) {
			mNegativeButtonText = text;
			mNegativeButtonListener = listener;
			return this;
		}

		public Builder setCancelable(boolean cancelable) {
			mCancelable = cancelable;
			return this;
		}

		public AlertDialog create() {
			final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
					.setCancelable(mCancelable).create();

			View myView = LayoutInflater.from(mContext).inflate(
					R.layout.costume_dialog, null);
			alertDialog.setView(myView, 0, 0, 0, 0);

			TextView tv_title = (TextView) myView
					.findViewById(R.id.textView_title);
			ImageView iv_icon = (ImageView) myView
					.findViewById(R.id.imageView_icon);
			View title_divider = myView.findViewById(R.id.title_divider);
			TextView tv_message = (TextView) myView
					.findViewById(R.id.textView_message);
			LinearLayout main_view = (LinearLayout) myView
					.findViewById(R.id.dialog_view);
			View content_divider = myView.findViewById(R.id.content_divider);
			View button_divider = myView.findViewById(R.id.button_divider);
			Button button_cancel = (Button) myView
					.findViewById(R.id.button_cancel);
			Button button_ok = (Button) myView.findViewById(R.id.button_ok);

			if (mTitle != null) {
				tv_title.setText(mTitle);
			} else {
				tv_title.setVisibility(View.GONE);
			}

			if (mIconId < 0 && mIcon == null) {
				iv_icon.setVisibility(View.GONE);
			} else if (mIcon != null) {
				iv_icon.setImageDrawable(mIcon);
			} else {
				iv_icon.setImageResource(mIconId);
			}

			if (mTitle == null && mIconId < 0 && mIcon == null) {
				title_divider.setVisibility(View.GONE);
			}

			if (mView != null) {
				main_view.removeAllViews();
				main_view.addView(mView);
			} else {
				tv_message.setText(mMessage);
			}

			if (mPositiveButtonText == null) {
				button_ok.setVisibility(View.GONE);
				button_divider.setVisibility(View.GONE);
			} else {
				button_ok.setText(mPositiveButtonText);
				button_ok
						.setOnClickListener(new android.view.View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (mPositiveButtonListener == null
										|| mPositiveButtonListener.onClick(
												alertDialog,
												AlertDialog.BUTTON_POSITIVE)) {
									alertDialog.cancel();
								}
							}
						});
			}

			if (mNegativeButtonText == null) {
				button_cancel.setVisibility(View.GONE);
				button_divider.setVisibility(View.GONE);
			} else {
				button_cancel.setText(mNegativeButtonText);
				button_cancel
						.setOnClickListener(new android.view.View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (mNegativeButtonListener == null
										|| mNegativeButtonListener.onClick(
												alertDialog,
												AlertDialog.BUTTON_NEGATIVE)) {
									alertDialog.cancel();
								}
							}
						});
			}

			if (mPositiveButtonText == null && mNegativeButtonText == null) {
				content_divider.setVisibility(View.GONE);
			}

			return alertDialog;
		}

		public AlertDialog show() {
			AlertDialog dialog = create();
			dialog.show();
			return dialog;
		}
	}

	public static interface OnClickListener {
		public boolean onClick(DialogInterface dialog, int which);
	}

}
