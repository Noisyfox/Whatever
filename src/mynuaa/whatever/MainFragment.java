package mynuaa.whatever;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import mynuaa.whatever.DataSource.ImageLoadTask;
import mynuaa.whatever.DataSource.ImageLoadTask.OnImageLoadListener;
import mynuaa.whatever.DataSource.ContactSyncTask;
import mynuaa.whatever.DataSource.MessageData;
import mynuaa.whatever.DataSource.MessageGetTask;
import mynuaa.whatever.DataSource.MessageGetTask.OnMessageGetListener;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainFragment extends SherlockFragment implements
		OnItemClickListener, OnRefreshListener<ListView>,
		OnLastItemVisibleListener, OnScrollListener, OnMessageGetListener,
		OnImageLoadListener {
	private static final String TASK_TAG = "task_main_fragment";

	private PullToRefreshListView rootListView;
	private ListView listView;
	private LinkedList<MessageData> currentData = new LinkedList<MessageData>();// 当前视图显示的数据
	private CustomAdapter customadapter = new CustomAdapter();// 自定义适配器
	private View loadingView;// 加载视图的布局
	private View fakeFootView;

	private View toTopView;

	private boolean mMessageGroupIsContact = false;
	private boolean mMessageGroupIsVisible = true;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.mainpage, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		FragmentActivity a = this.getActivity();

		if (MainActivity.class.isInstance(a)) {
			MainActivity ma = (MainActivity) a;
			boolean drawerOpen = ma.mDrawerLayout.isDrawerOpen(ma.mDrawerList);
			mMessageGroupIsVisible = !drawerOpen;

			updateSwitchIcon(menu.findItem(R.id.switch_group));
		}
	}

	private void updateSwitchIcon(MenuItem item) {
		item.setVisible(mMessageGroupIsVisible);
		if (mMessageGroupIsVisible) {
			if (mMessageGroupIsContact) {
				item.setIcon(R.drawable.ic_location);
				item.setTitle(R.string.switch_location);
			} else {
				item.setIcon(R.drawable.ic_contact);
				item.setTitle(R.string.switch_contact);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.switch_group:
			if (!mMessageGroupIsContact
					&& !ContactSyncTask.contactEnabled(getActivity())) {
				Dialog alertDialog = new MyAlertDialog.Builder(getActivity())
						.setMessage(
								"\u3000\u3000基于通讯录的状态匹配需要开启通讯录匹配功能。你可以在设置中点击\"匹配手机通讯录\"来启用它。")
						.setPositiveButton("现在去开",
								new MyAlertDialog.OnClickListener() {
									@Override
									public boolean onClick(
											DialogInterface dialog, int which) {
										((MainActivity) getActivity())
												.jumpToSettings();
										return true;
									}
								}).setNegativeButton("算了吧", null).create();
				alertDialog.show();
			} else {
				returnTop();
				mMessageGroupIsContact = !mMessageGroupIsContact;
				clearLoading();
				updateSwitchIcon(item);
				loadCachedMessage(mMessageGroupIsContact ? MessageData.MESSAGE_FILTER_CONTACT
						: MessageData.MESSAGE_FILTER_LOCATION);
				if (currentData.isEmpty()) {
					updateCurrendData(true);
				}
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean isLoading = false;// 是否加载过,控制加载次数

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("firstStart", false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);

		toTopView = rootView.findViewById(R.id.button_return_top);
		toTopView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				returnTop();
			}
		});

		fakeFootView = LayoutInflater.from(getActivity()).inflate(
				R.layout.foot_main_fake, null);
		loadingView = LayoutInflater.from(getActivity()).inflate(
				R.layout.footer, null);
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_more);

		rootListView = (PullToRefreshListView) rootView
				.findViewById(R.id.main_list);
		listView = rootListView.getRefreshableView();
		// 载入缓存的数据
		loadCachedMessage(mMessageGroupIsContact ? MessageData.MESSAGE_FILTER_CONTACT
				: MessageData.MESSAGE_FILTER_LOCATION);
		// 添加底部加载视图
		listView.addFooterView(loadingView);
		listView.addFooterView(fakeFootView, null, false);
		// 初始化适配器
		// customadapter = new CustomAdapter();

		rootListView.setAdapter(customadapter);
		// 滚动条监听
		rootListView.setOnItemClickListener(this);
		rootListView.setOnScrollListener(this);

		rootView.findViewById(R.id.button_write_message).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent();
						intent.setClass(getActivity(),
								WriteMessageActivity.class);
						startActivity(intent);
						getActivity().overridePendingTransition(
								R.anim.slide_in_bottom, R.anim.stay);
					}
				});

		rootListView.setOnRefreshListener(this);
		rootListView.setOnLastItemVisibleListener(this);

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		setHasOptionsMenu(true);

		clearLoading();

		if ((savedInstanceState == null || savedInstanceState.getBoolean(
				"firstStart", true)) && currentData.isEmpty()) {
			updateCurrendData(true);
		}

		return rootView;
	}

	public void returnTop() {
		listView.smoothScrollToPosition(0);
	}

	private void loadCachedMessage(int filter) {
		List<MessageData> ms = MessageData.getCachedMessages(filter);
		synchronized (currentData) {
			currentData.clear();
			for (MessageData md : ms) {
				currentData.add(md);
			}
		}
		customadapter.notifyDataSetChanged();
	}

	// 添加List元素
	private void updateCurrendData(boolean getNew) {
		if (isLoading) {
			return;
		}

		if (mMessageGroupIsContact
				&& !ContactSyncTask.contactEnabled(getActivity())) {
			Dialog alertDialog = new MyAlertDialog.Builder(getActivity())
					.setMessage(
							"\u3000\u3000基于通讯录的状态匹配需要开启通讯录匹配功能。你可以在设置中点击\"匹配手机通讯录\"来启用它。")
					.setPositiveButton("现在去开",
							new MyAlertDialog.OnClickListener() {
								@Override
								public boolean onClick(DialogInterface dialog,
										int which) {
									((MainActivity) getActivity())
											.jumpToSettings();
									return true;
								}
							}).setNegativeButton("算了吧", null).create();
			alertDialog.show();
			clearLoading();
			return;
		}

		System.out.println("开始加载..");
		isLoading = true;
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_loading);

		String prev = null;
		if (!getNew) {
			synchronized (currentData) {
				if (!currentData.isEmpty()) {
					MessageData md = currentData.get(currentData.size() - 1);
					if (md != null)
						prev = md.cid;
				}
			}
		}

		WhateverApplication
				.getMainTaskManager()
				.startTask(
						new MessageGetTask(
								TASK_TAG,
								prev,
								mMessageGroupIsContact ? MessageData.MESSAGE_FILTER_CONTACT
										: MessageData.MESSAGE_FILTER_LOCATION,
								this));

	}

	class CustomAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			synchronized (currentData) {
				return currentData.size();
			}
		}

		@Override
		public Object getItem(int position) {
			synchronized (currentData) {
				return currentData.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = LayoutInflater.from(
						MainFragment.this.getActivity()).inflate(
						R.layout.message_list_item, null);

			MessageData md = (MessageData) getItem(position);

			TextView textView_context = (TextView) convertView
					.findViewById(R.id.textView_content);
			TextView textView_time = (TextView) convertView
					.findViewById(R.id.textView_time);
			ImageView imageView_image = (ImageView) convertView
					.findViewById(R.id.imageView_image);
			TriangleCornerView triangleCornerView_corner = (TriangleCornerView) convertView
					.findViewById(R.id.TriangleCornerView_corner);

			textView_context.setText(md.content);
			textView_time.setText(md.time);

			triangleCornerView_corner.setColor(MessageTheme.getColor(
					md.background_color_index, MessageTheme.COLOR_CORNER));

			if (!md.image_cid.isEmpty()) {
				imageView_image.setVisibility(View.VISIBLE);
				if (md.image_load_fail) {
					imageView_image.setBackgroundColor(0xffcccccc);
					imageView_image.setImageResource(R.drawable.image_broken_n);
					imageView_image.setScaleType(ScaleType.CENTER);
				} else {
					imageView_image
							.setBackgroundColor(md.image_prev == null ? 0xffcccccc
									: 0);
					imageView_image.setImageBitmap(md.image_prev);
					imageView_image.setScaleType(ScaleType.FIT_CENTER);
				}

				if (md.image_prev == null && !md.image_load_fail) {
					WhateverApplication.getMainTaskManager().startTask(
							new ImageLoadTask(TASK_TAG, 0, md.image_cid,
									"small", MainFragment.this));
				}
			} else {
				imageView_image.setVisibility(View.GONE);
			}

			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg1 == this.loadingView) {
			updateCurrendData(false);
		} else {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), MessageActivity.class);

			MessageData md = (MessageData) arg0.getAdapter().getItem(arg2);
			intent.putExtra("message", md);

			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_right,
					R.anim.stay);
		}
	}

	@Override
	public void onLastItemVisible() {
		// Toast.makeText(MainFragment.this.getActivity(),
		// "第 " + currentPage + " 页", Toast.LENGTH_LONG).show();
		updateCurrendData(false);
	}

	@Override
	public void onRefresh(final PullToRefreshBase<ListView> refreshView) {
		if (isLoading) {
			refreshView.post(new Runnable() {
				@Override
				public void run() {
					refreshView.onRefreshComplete();
				}
			});
		} else {
			updateCurrendData(true);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem > 0) {
			toTopView.setVisibility(View.VISIBLE);
		} else {
			toTopView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	private void mergeMessage(String prevId, List<MessageData> messages) {
		boolean addNew = false;

		if (messages != null && !messages.isEmpty()) {
			synchronized (currentData) {
				if (prevId == null) {// 在头部追加
					messages.removeAll(currentData);
					addNew = messages.size() > 0;
					if (addNew || currentData.size() > 20) {
						Queue<MessageData> tmpQueue = new LinkedList<MessageData>();

						for (MessageData md : messages) {
							if (tmpQueue.size() >= 20) {
								break;
							}
							tmpQueue.offer(md);
						}
						while (tmpQueue.size() < 20 && !currentData.isEmpty()) {
							tmpQueue.offer(currentData.poll());
						}

						currentData.clear();
						while (!tmpQueue.isEmpty()) {
							currentData.offer(tmpQueue.poll());
						}
					}
				} else {// 在尾部追加
					messages.removeAll(currentData);
					for (MessageData md : messages) {
						addNew = true;
						currentData.addLast(md);
					}
				}
			}
		}

		if (!addNew) {
			Toast.makeText(getActivity(), "没有更多状态了", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onMessageGet(int result, int filter, String prevId,
			List<MessageData> messages) {

		if (result == MessageGetTask.GET_SUCCESS
				&& ((mMessageGroupIsContact && filter == MessageData.MESSAGE_FILTER_CONTACT) || (!mMessageGroupIsContact && filter == MessageData.MESSAGE_FILTER_LOCATION))) {
			mergeMessage(prevId, messages);
			// 更新
			customadapter.notifyDataSetChanged();
			clearLoading();
		}

	}

	private void clearLoading() {
		((TextView) loadingView.findViewById(R.id.textView_footer))
				.setText(R.string.footer_more);
		// 进入下一页，此时视图未加载.
		isLoading = false;

		rootListView.onRefreshComplete();
	}

	@Override
	public void onImageLoaded(int taskCode, String cid, String size,
			Bitmap image) {
		boolean refresh = false;
		synchronized (currentData) {
			for (MessageData md : currentData) {
				if (md.image_cid.equals(cid)) {
					md.image_load_fail = image == null;

					if ("small".equals(size)) {
						md.image_prev = image;
					} else {
						md.image = image;
					}
					refresh = true;
					// break;
				}
			}
		}
		if (refresh)
			customadapter.notifyDataSetChanged();
	}
}
