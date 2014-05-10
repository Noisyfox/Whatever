package mynuaa.whatever;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAlertDialog {

	public static AlertDialog creatAlertDialog(Context context, String title,
			View view, String positiveButton,
			final OnClickListener positiveListener, String negativeButton,
			final OnClickListener negativeListener) {

		final AlertDialog alertDialog = new AlertDialog.Builder(context)
				.create();
		View myView = LayoutInflater.from(context).inflate(
				R.layout.costume_dialog, null);
		alertDialog.setView(myView, 0, 0, 0, 0);

		TextView tv_title = (TextView) myView.findViewById(R.id.textView_title);
		LinearLayout main_view = (LinearLayout) myView
				.findViewById(R.id.dialog_view);
		Button button_cancel = (Button) myView.findViewById(R.id.button_cancel);
		Button button_ok = (Button) myView.findViewById(R.id.button_ok);

		if (title != null) {
			tv_title.setText(title);
		}

		if (view != null) {
			main_view.addView(view);
		}

		if (positiveButton != null) {
			button_ok.setText(positiveButton);
		}

		if (negativeButton != null) {
			button_cancel.setText(negativeButton);
		}

		button_ok.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (positiveListener == null
						|| positiveListener.onClick(alertDialog,
								AlertDialog.BUTTON_POSITIVE)) {
					alertDialog.cancel();
				}
			}

		});

		button_cancel
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (negativeListener == null
								|| negativeListener.onClick(alertDialog,
										AlertDialog.BUTTON_NEGATIVE)) {
							alertDialog.cancel();
						}
					}

				});

		return alertDialog;
	}

	public static interface OnClickListener {
		public boolean onClick(DialogInterface dialog, int which);
	}

}
