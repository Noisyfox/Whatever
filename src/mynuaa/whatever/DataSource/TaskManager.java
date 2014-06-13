package mynuaa.whatever.DataSource;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class TaskManager {
	public static final String TAG_GLOBAL = "Global";

	private final Queue<Task> mWaitingTask = new LinkedList<Task>();
	private final Queue<Task> mFinishedTask = new LinkedList<Task>();

	private int mCurrentTaskCount = 0;
	private int mMaxTaskCount = -1;

	private final Object mSyncObj = new Object();

	private Handler mHandler;

	private final HashMap<String, Boolean> mActivateTag = new HashMap<String, Boolean>();

	private static class TaskHandler extends Handler {
		WeakReference<TaskManager> mTaskManager;

		public TaskHandler(TaskManager tm) {
			mTaskManager = new WeakReference<TaskManager>(tm);
		}

		public TaskHandler(TaskManager tm, Looper l) {
			super(l);
			mTaskManager = new WeakReference<TaskManager>(tm);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			TaskManager tm = mTaskManager.get();

			if (tm != null) {
				tm.callback();
			}
		}
	}

	public TaskManager() {
		mHandler = new TaskHandler(this);
		mActivateTag.put(TAG_GLOBAL, Boolean.TRUE);
	}

	public TaskManager(Looper l) {
		mHandler = new TaskHandler(this, l);
		mActivateTag.put(TAG_GLOBAL, Boolean.TRUE);
	}

	public void setMaxTask(int max) {
		mMaxTaskCount = max;
	}

	public void startTask(Task task) {
		synchronized (mSyncObj) {
			mWaitingTask.offer(task);

			dispatchAllTask();
		}
	}

	public void activateTag(String tag) {
		synchronized (mSyncObj) {
			mActivateTag.put(tag, Boolean.TRUE);
		}
	}

	public void deactivateTag(String tag) {
		synchronized (mSyncObj) {
			mActivateTag.put(tag, Boolean.FALSE);
		}
	}

	/*-**********************************************************************-*/

	private class TaskThread extends Thread {
		Task currentTask;

		public TaskThread(Task task) {
			currentTask = task;
		}

		@Override
		public void run() {

			while (currentTask != null) {
				if (checkTagEnabled(currentTask)) {
					currentTask.doTask();

					synchronized (mSyncObj) {// 任务完成，开始准备推送回调并执行下一任务
						mFinishedTask.offer(currentTask);
						currentTask = peekOneTask();
						dispatchAllTask();
						postCallback();
					}
				} else {
					synchronized (mSyncObj) {// 任务完成，开始准备推送回调并执行下一任务
						currentTask = peekOneTask();
						dispatchAllTask();
						postCallback();
					}
				}
			}
			synchronized (mSyncObj) {
				mCurrentTaskCount--;// 计数器减1
			}
		}

	}

	private boolean checkTagEnabled(Task t) {
		synchronized (mSyncObj) {
			Boolean b = mActivateTag.get(t.getTag());

			return (b != null && b.equals(Boolean.TRUE));
		}
	}

	private Task peekOneTask() {
		synchronized (mSyncObj) {
			while (!mWaitingTask.isEmpty()
					&& (mMaxTaskCount <= 0 || mCurrentTaskCount < mMaxTaskCount)) {
				Task t = mWaitingTask.poll();
				if (checkTagEnabled(t)) {
					return t;
				}
			}
		}
		return null;
	}

	private void dispatchAllTask() {
		synchronized (mSyncObj) {
			while (!mWaitingTask.isEmpty()
					&& (mMaxTaskCount <= 0 || mCurrentTaskCount < mMaxTaskCount)) {
				Task t = mWaitingTask.poll();
				if (checkTagEnabled(t)) {
					mCurrentTaskCount++;
					new TaskThread(t).start();
				}
			}
		}
	}

	private void postCallback() {
		mHandler.sendEmptyMessage(0);
	}

	private void callback() {
		Queue<Task> mt = new LinkedList<Task>();
		synchronized (mSyncObj) {
			while (!mFinishedTask.isEmpty()) {
				mt.offer(mFinishedTask.poll());
			}
		}

		while (!mt.isEmpty()) {
			Task task = mt.poll();
			if (checkTagEnabled(task)) {
				task.callback();
			}
		}
	}
}
