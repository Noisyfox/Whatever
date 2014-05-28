package mynuaa.whatever;

import mynuaa.whatever.DataSource.TraceGetTask;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class PathFragment extends SherlockFragment implements OnClickListener/*
																			 * ,
																			 * OnTraceGetListener
																			 */{
	// private static final String TASK_TAG = "task_path_fragment";

	View button_bad, button_good, button_comment, button_send, button_report,
			button_pm, button_who;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_path, container,
				false);

		button_bad = rootView.findViewById(R.id.button_bad);
		button_good = rootView.findViewById(R.id.button_good);
		button_comment = rootView.findViewById(R.id.button_comment);
		button_send = rootView.findViewById(R.id.button_send);
		button_report = rootView.findViewById(R.id.button_report);
		button_pm = rootView.findViewById(R.id.button_pm);
		button_who = rootView.findViewById(R.id.button_who);

		// WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		setupButtons();

		return rootView;
	}

	private void setupButtons() {
		button_bad.setOnClickListener(this);
		button_good.setOnClickListener(this);
		button_comment.setOnClickListener(this);
		button_send.setOnClickListener(this);
		button_report.setOnClickListener(this);
		button_pm.setOnClickListener(this);
		button_who.setOnClickListener(this);
	}

	private void show_list(int filter) {
		Intent i = new Intent();
		i.setClass(getActivity(), TraceListActivity.class);
		i.putExtra("trace", filter);
		getActivity().startActivity(i);
		getActivity().overridePendingTransition(R.anim.slide_in_right,
				R.anim.stay);
	}

	@Override
	public void onClick(View v) {
		if (v == button_bad) {
			show_list(TraceGetTask.FILTER_BAD);
		} else if (v == button_good) {
			show_list(TraceGetTask.FILTER_GOOD);
		} else if (v == button_comment) {
			show_list(TraceGetTask.FILTER_COMMENT);
		} else if (v == button_send) {
			show_list(TraceGetTask.FILTER_SEND);
		} else if (v == button_report) {
			show_list(TraceGetTask.FILTER_REPORT);
		} else if (v == button_pm) {
			show_list(TraceGetTask.FILTER_PM);
		} else if (v == button_who) {
			show_list(TraceGetTask.FILTER_WHO);
		}
	}

	/*
	 * @Override public void onTraceGet(int result, int filter, String prevId,
	 * List<MessageData> messages) { }
	 */
}
