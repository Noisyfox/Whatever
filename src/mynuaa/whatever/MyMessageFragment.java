package mynuaa.whatever;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class MyMessageFragment extends SherlockListFragment {

	private View loadingView;// 加载视图的布局

	Handler loadingHandler = new MyHandler(this);

	private static class MyHandler extends Handler {
		WeakReference<MyMessageFragment> mMyMessageFragment;

		public MyHandler(MyMessageFragment mf) {
			mMyMessageFragment = new WeakReference<MyMessageFragment>(mf);
		}

		@Override
		public void handleMessage(Message msg) {

			MyMessageFragment myMessageFragment = mMyMessageFragment.get();

			if (myMessageFragment == null)
				return;

		};
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		loadingView = LayoutInflater.from(activity).inflate(R.layout.footer,
				null);
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_more);

		this.setListAdapter(new MyMessageAdapter());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		this.getListView().removeFooterView(loadingView);
		this.getListView().addFooterView(loadingView);
	}

	class MyMessageAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 10;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			if (convertView == null)
				convertView = LayoutInflater.from(
						MyMessageFragment.this.getActivity()).inflate(
						R.layout.mymessage_list_item, null);

			return convertView;
		}

	}

}
