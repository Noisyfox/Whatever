package mynuaa.whatever.DataSource;

import java.util.HashMap;
import java.util.Map;

import mynuaa.whatever.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

public class CommentPostTask extends Task {

	public static final int POST_SUCCESS = 0;
	public static final int POST_FAIL_INNER_ERROR = 1;
	public static final int POST_FAIL_DELETED = 2;

	private final CommentData mComment;
	private final OnCommentPostListener mOnCommentPostListener;

	private int mResult = POST_SUCCESS;

	public CommentPostTask(String tag, CommentData comment,
			OnCommentPostListener onCommentPostListener) {
		super(tag);

		mComment = comment;
		mOnCommentPostListener = onCommentPostListener;
	}

	@Override
	public void doTask() {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("session", mSessionId);
		params.put("nid", mComment.message_cid);
		params.put("content", Util.messageEncode(mComment.content));
		params.put("reply_to", String.valueOf(mComment.replyTo));

		Log.d("reply", "reply_to:" + String.valueOf(mComment.replyTo));

		String result = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL_COMMENT_POST, params.entrySet());

		if (TextUtils.isEmpty(result)) {
			mResult = POST_FAIL_INNER_ERROR;
			return;
		}

		Log.d("http cmtpost", result);

		try {
			JSONTokener jsonParser = new JSONTokener(result);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();

			int errorCode = jsonObj.getInt("ErrorCode");

			if (errorCode == 0) {
				JSONObject commentJson = jsonObj.getJSONObject("Comment");
				CommentData.parse(commentJson, mComment);
				mResult = POST_SUCCESS;
			} else if (errorCode == 404) {
				mResult = POST_FAIL_DELETED;
			} else {
				mResult = POST_FAIL_INNER_ERROR;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			mResult = POST_FAIL_INNER_ERROR;
		}
	}

	@Override
	public void callback() {
		if (mOnCommentPostListener != null) {
			mOnCommentPostListener.onCommentPost(mResult, mComment);
		}
	}

	public static interface OnCommentPostListener {
		public void onCommentPost(int result, CommentData comment);
	}
}
