package mynuaa.whatever.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class CommentGetTask extends Task {

	public static final int GET_SUCCESS = 0;
	public static final int GET_FAIL_INNER_ERROR = 1;
	public static final int GET_FAIL_DELETED = 2;

	private final String mMessageId;
	private final String mPrevId;
	private final OnCommentGetListener mOnCommentGetListener;

	private int mResult = GET_SUCCESS;
	List<CommentData> mComments = null;

	public CommentGetTask(String tag, String message_cid, String prevComment,
			OnCommentGetListener onCommentGetListener) {
		super(tag);

		mMessageId = message_cid;
		mPrevId = prevComment;
		mOnCommentGetListener = onCommentGetListener;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("nid", mMessageId);
		if (mPrevId != null) {
			params.put("cid", mPrevId);
		}

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_COMMENT_GET, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = GET_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http cmtget", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				JSONArray ja = jsonObj.getJSONArray("CommentList");

				List<CommentData> comments = new ArrayList<CommentData>();

				int size = ja.length();
				for (int i = 0; i < size; i++) {
					JSONObject commentObj = ja.getJSONObject(i);
					CommentData cd = CommentData.parse(commentObj, null);
					comments.add(cd);
				}

				mComments = comments;
				mResult = GET_SUCCESS;
			} else if (errorCode == 404) {
				mResult = GET_FAIL_DELETED;
			} else {
				mResult = GET_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = GET_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnCommentGetListener != null) {
			mOnCommentGetListener.onCommentGet(mResult, mMessageId, mPrevId,
					mComments);
		}
	}

	public static interface OnCommentGetListener {
		public void onCommentGet(int result, String message_cid, String prevId,
				List<CommentData> comments);
	}
}
