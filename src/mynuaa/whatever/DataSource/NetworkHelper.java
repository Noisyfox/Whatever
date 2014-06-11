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
			// 设置超时
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
	private static String boundary = "****************fD4fH3gL0hK7aI6"; // ���ݷָ���
	private static String lineEnd = System.getProperty("line.separator"); // The
																			// value
																			// is

	// "\r\n" in
	// Windows.

	/*
	 * �ϴ�ͼƬ���ݣ���ʽ��ο�HTTP Э���ʽ��
	 * ������Photos.upload�еġ�������á�http://wiki.dev.renren.com/
	 * wiki/Photos.upload#.E7.A8.8B.E5.BA.8F.E8.B0.83.E7.94.A8 �����ʽ���͵ķǳ�������
	 * ��ʽ������ʾ�� --****************fD4fH3hK7aI6 Content-Disposition: form-data;
	 * name="upload_file"; filename="apple.jpg" Content-Type: image/jpeg
	 * 
	 * ������ļ������ݣ�������������ʽ
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
			// ����ͼƬ����
			output.writeBytes(split.toString());
			// output.write(file.getData(), 0, file.getData().length);
			image.compress(Bitmap.CompressFormat.PNG, 100, output);
			output.writeBytes(lineEnd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * �������ֶ����ݣ���ʽ��ο�HTTP Э���ʽ����FireBug����ץȡ��������ݣ���(�Ա��ϴ������Ӧ�Ĳ���ֵ) ��ʽ������ʾ��
	 * --****************fD4fH3hK7aI6 Content-Disposition: form-data;
	 * name="action" // һ���У������� upload
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
			output.write(sb.toString().getBytes());// ���ͱ��ֶ�����
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
			conn.setDoInput(true); // ��������
			conn.setDoOutput(true); // �������
			conn.setUseCaches(false); // ��ʹ��Cache
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Content-Type", multipart_form_data
					+ "; boundary=" + boundary);

			conn.connect();
			output = new DataOutputStream(conn.getOutputStream());

			addImageContent("image", "image.png", image, output); // ���ͼƬ����

			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put("session", session);
			addFormField(params.entrySet(), output); // ��ӱ��ֶ�����

			output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);// ���ݽ�����־
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
			// ͳһ�ͷ���Դ
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
	 * �ֶ����DNS���棬���ƹ����ڵ�DNS��Ⱦ
	 * 
	 * @param host
	 * @param ip
	 * @return �Ƿ�ɹ�
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
