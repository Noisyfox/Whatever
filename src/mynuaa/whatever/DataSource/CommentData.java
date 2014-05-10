package mynuaa.whatever.DataSource;

import mynuaa.whatever.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class CommentData {
	public String message_cid;
	public String cid;
	public String content;
	public String time;
	public int user;
	public int replyTo;

	public String user_display;

	public static CommentData parse(JSONObject commentObj, CommentData convert)
			throws JSONException {
		String cid = commentObj.getString("cid");
		String message_cid = commentObj.getString("nid");
		String content = commentObj.getString("content");
		String time = commentObj.getString("time");
		String userStr = commentObj.getString("rid");
		String replyToStr = commentObj.getString("reply_to");

		time = Util.formatTime(time);
		int user = Integer.parseInt(userStr);
		int replyTo = Integer.parseInt(replyToStr);
		String user_display = user == 0 ? "楼主" : "用户" + user;
		if (replyTo != 0) {
			user_display = user_display + " 回复 用户" + replyTo;
		}

		if (convert == null) {
			convert = new CommentData();
		}

		convert.cid = cid;
		convert.message_cid = message_cid;
		convert.content = Util.messageDecode(content);
		convert.time = time;
		convert.user = user;
		convert.replyTo = replyTo;
		convert.user_display = user_display;

		return convert;
	}
}
