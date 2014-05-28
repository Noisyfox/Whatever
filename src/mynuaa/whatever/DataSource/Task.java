package mynuaa.whatever.DataSource;

public abstract class Task {
	private final String mTag;
	protected final String mSessionId;

	public Task(String tag) {
		mTag = tag;
		UserSession us = UserSession.getCurrentSession();
		if (us != null) {
			mSessionId = us.mSession;
		} else {
			mSessionId = null;
		}
	}

	protected final String getTag() {
		return mTag;
	}

	public abstract void doTask();

	public abstract void callback();
}
