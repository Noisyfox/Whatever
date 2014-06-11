package mynuaa.whatever.DataSource;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.util.Log;

public class NetworkHelper {
	static final String STR_SERVER_URL = "http://my.nuaa.edu.cn/zfjapp/index.php/home/";
	static final String STR_SERVER_URL_LOGIN = STR_SERVER_URL + "Login/login";
	static final String STR_SERVER_URL_USERINFO_UPDATE = STR_SERVER_URL
			+ "User/updateinfo/";
	static final String STR_SERVER_URL_MESSAGE_POST = STR_SERVER_URL
			+ "News/post/";
	static final String STR_SERVER_URL_IMAGE_UPLOAD = STR_SERVER_URL
			+ "Image/upload";
	static final String STR_SERVER_URL_MESSAGE_GET = STR_SERVER_URL
			+ "News/get";
	static final String STR_SERVER_URL_COMMENT_POST = STR_SERVER_URL
			+ "Comment/post";
	static final String STR_SERVER_URL_COMMENT_GET = STR_SERVER_URL
			+ "Comment/get";
	static final String STR_SERVER_URL_MANNER_PUT = STR_SERVER_URL
			+ "news/manner";
	static final String STR_SERVER_URL_TRACE_GET = STR_SERVER_URL + "trace/";
	static final String STR_SERVER_URL_CONTACT_SYNC = STR_SERVER_URL
			+ "User/updatecontact";
	static final String STR_SERVER_URL_REPORT = STR_SERVER_URL + "report/post/";
	static final String STR_SERVER_URL_PM_REQUIRESESSION = STR_SERVER_URL
			+ "Pm/getsid";
	static final String STR_SERVER_URL_PM_POST = STR_SERVER_URL + "Pm/post";
	static final String STR_SERVER_URL_PM_GET = STR_SERVER_URL + "Pm/get";
	static final String STR_SERVER_URL_NOTIFY_GET = STR_SERVER_URL
			+ "Notification/get";
	static final String STR_SERVER_URL_NOTIFY_CHECK = STR_SERVER_URL
			+ "Notification/check";
	static final String STR_SERVER_URL_WHO_ASK = STR_SERVER_URL + "Who/post";
	static final String STR_SERVER_URL_WHO_REPLY = STR_SERVER_URL + "Who/reply";
	static final String STR_SERVER_URL_UPDATE = "https://whatever-noisyfox.rhcloud.com/update";

	public static String doHttpRequest(String url,
			Set<Entry<Object, Object>> data) {

		try {
			List<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
			for (Map.Entry<Object, Object> entry : data) {
				postData.add(new BasicNameValuePair(entry.getKey().toString(),
						entry.getValue().toString()));
			}

			DefaultHttpClient httpClient = new DefaultHttpClient();
			// Represents a collection of HTTP protocol and framework parameters
			HttpParams params = null;
			params = httpClient.getParams();
			// 璁剧疆瓒呮椂
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 35000);

			HttpPost post = new HttpPost(url);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData,
					HTTP.UTF_8);
			post.setEntity(entity);

			HttpResponse response = httpClient.execute(post);

			int resultCode = response.getStatusLine().getStatusCode();
			Log.d("http", "result:" + resultCode);
			if (resultCode != HttpStatus.SC_OK
					&& resultCode != HttpStatus.SC_UNAUTHORIZED
					&& resultCode != HttpStatus.SC_NOT_FOUND) {
				return null;
			}

			HttpEntity httpEntity = response.getEntity();
			InputStream is = httpEntity.getContent();
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private static String multipart_form_data = "multipart/form-data";
	private static String twoHyphens = "--";
	private static String boundary = "****************fD4fH3gL0hK7aI6"; // 数据分隔符
	private static String lineEnd = System.getProperty("line.separator"); // The
																			// value
																			// is

	// "\r\n" in
	// Windows.

	/*
	 * 上传图片内容，格式请参考HTTP 协议格式。
	 * 人人网Photos.upload中的”程序调用“http://wiki.dev.renren.com/
	 * wiki/Photos.upload#.E7.A8.8B.E5.BA.8F.E8.B0.83.E7.94.A8 对其格式解释的非常清晰。
	 * 格式如下所示： --****************fD4fH3hK7aI6 Content-Disposition: form-data;
	 * name="upload_file"; filename="apple.jpg" Content-Type: image/jpeg
	 * 
	 * 这儿是文件的内容，二进制流的形式
	 */
	private static void addImageContent(String formName, String fileName,
			Bitmap image, DataOutputStream output) {
		StringBuilder split = new StringBuilder();
		split.append(twoHyphens + boundary + lineEnd);
		split.append("Content-Disposition: form-data; name=\"" + formName
				+ "\"; filename=\"" + fileName + "\"" + lineEnd);
		split.append("Content-Type: image/png" + lineEnd);
		split.append(lineEnd);
		try {
			// 发送图片数据
			output.writeBytes(split.toString());
			// output.write(file.getData(), 0, file.getData().length);
			image.compress(Bitmap.CompressFormat.PNG, 100, output);
			output.writeBytes(lineEnd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * 构建表单字段内容，格式请参考HTTP 协议格式（用FireBug可以抓取到相关数据）。(以便上传表单相对应的参数值) 格式如下所示：
	 * --****************fD4fH3hK7aI6 Content-Disposition: form-data;
	 * name="action" // 一空行，必须有 upload
	 */
	private static void addFormField(Set<Map.Entry<Object, Object>> params,
			DataOutputStream output) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Object, Object> param : params) {
			sb.append(twoHyphens + boundary + lineEnd);
			sb.append("Content-Disposition: form-data; name=\""
					+ param.getKey() + "\"" + lineEnd);
			sb.append(lineEnd);
			sb.append(param.getValue());
			sb.append(lineEnd);
		}
		try {
			output.write(sb.toString().getBytes());// 发送表单字段数据
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String uploadImage(String actionUrl, String session,
			Bitmap image) {
		HttpURLConnection conn = null;
		DataOutputStream output = null;
		BufferedReader input = null;

		try {
			URL url = new URL(actionUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(120000);
			conn.setDoInput(true); // 允许输入
			conn.setDoOutput(true); // 允许输出
			conn.setUseCaches(false); // 不使用Cache
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Content-Type", multipart_form_data
					+ "; boundary=" + boundary);

			conn.connect();
			output = new DataOutputStream(conn.getOutputStream());

			addImageContent("image", "image.png", image, output); // 添加图片内容

			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put("session", session);
			addFormField(params.entrySet(), output); // 添加表单字段内容

			output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);// 数据结束标志
			output.flush();

			int resultCode = conn.getResponseCode();
			Log.d("http image upload", "result:" + resultCode);
			if (resultCode != HttpStatus.SC_OK
					&& resultCode != HttpStatus.SC_UNAUTHORIZED
					&& resultCode != HttpStatus.SC_NOT_FOUND)
				return null;

			input = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String oneLine;
			while ((oneLine = input.readLine()) != null) {
				response.append(oneLine + lineEnd);
			}

			return response.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 统一释放资源
			try {
				if (output != null) {
					output.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (conn != null) {
				conn.disconnect();
			}
		}

		return null;
	}

	/**
	 * 手动添加DNS缓存，以绕过国内的DNS污染
	 * 
	 * @param host
	 * @param ip
	 * @return 是否成功
	 */
	public static boolean addDNSCache(String host, String ip) {
		try {
			InetAddress addr2[] = { InetAddress.getByName(ip) };

			Class<?> clazz = java.net.InetAddress.class;
			final Field cacheField = clazz.getDeclaredField("addressCache");
			cacheField.setAccessible(true);
			final Object o = cacheField.get(clazz);
			synchronized (o) {
				Class<?> clazz2 = o.getClass();
				Method method = clazz2.getMethod("put", String.class,
						InetAddress[].class);
				method.setAccessible(true);
				method.invoke(o, host, addr2);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
