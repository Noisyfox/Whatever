package mynuaa.whatever;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class NotifyFragment extends SherlockFragment {

	private NotifyAdapter notifyAdapter = new NotifyAdapter();// �Զ���������
	private View loadingView;// ������ͼ�Ĳ���
	private PullToRefreshListView rootListView;
	private ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_notify, container,
				false);

		loadingView = inflater.inflate(R.layout.footer, null);
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_more);

		rootListView = (PullToRefreshListView) rootView
				.findViewById(R.id.main_list);
		listView = rootListView.getRefreshableView();

		// ��ӵײ�������ͼ
		listView.addFooterView(loadingView);
		// ��ʼ��������

		rootListView.setAdapter(notifyAdapter);

		return rootView;
	}

	class NotifyAdapter extends BaseAdapter {

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
						NotifyFragment.this.getActivity()).inflate(
						R.layout.mymessage_list_item, null);

			return convertView;
		}

	}

}
