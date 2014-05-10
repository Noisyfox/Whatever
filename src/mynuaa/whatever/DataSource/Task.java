package mynuaa.whatever.DataSource;

public abstract class Task {
	private final String mTag;

	public Task(String tag) {
		mTag = tag;
	}

	protected final String getTag() {
		return mTag;
	}

	public abstract void doTask();

	public abstract void callback();
}
