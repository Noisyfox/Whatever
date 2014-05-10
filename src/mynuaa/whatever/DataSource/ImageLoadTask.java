package mynuaa.whatever.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mynuaa.whatever.DataSource.DataCenter.ImageCacheManager;

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
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageLoadTask extends Task {
	static final String STR_SERVER_URL = "http://my.nuaa.edu.cn/zfjapp/index.php/home/Image/load/";

	private final int mTaskCode;
	private final String mCid;
	private final String mSize;
	private final OnImageLoadListener mOnImageLoadListener;

	private Bitmap mResult = null;

	public ImageLoadTask(String tag, int taskCode, String imageCid,
			String size, OnImageLoadListener onImageLoadListener) {
		super(tag);
		mTaskCode = taskCode;
		mCid = imageCid;
		mSize = size;
		mOnImageLoadListener = onImageLoadListener;
	}

	@Override
	public void doTask() {
		mResult = loadBitmap(mCid, true, mSize);
	}

	protected static Bitmap loadBitmap(String cid, boolean checkCache,
			String size) {
		ImageCacheManager imageCacheManager = DataCenter.getImageCacheManager();
		Bitmap b = imageCacheManager.getImageCache(cid, size);
		if (b == null || !checkCache) {
			try {
				List<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
				postData.add(new BasicNameValuePair("session", UserSession
						.getCurrentSession().mSession));
				postData.add(new BasicNameValuePair("size", size));
				postData.add(new BasicNameValuePair("id", cid));

				DefaultHttpClient httpClient = new DefaultHttpClient();
				// Represents a collection of HTTP protocol and framework
				// parameters
				HttpParams httpParams = httpClient.getParams();
				// …Ë÷√≥¨ ±
				HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
				HttpConnectionParams.setSoTimeout(httpParams, 35000);

				HttpPost post = new HttpPost(STR_SERVER_URL);
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						postData, HTTP.UTF_8);
				post.setEntity(entity);

				HttpResponse response = httpClient.execute(post);

				int resultCode = response.getStatusLine().getStatusCode();
				Log.d("http image", "result:" + resultCode);
				if (resultCode == HttpStatus.SC_OK) {
					HttpEntity httpEntity = response.getEntity();
					InputStream is = httpEntity.getContent();
					b = BitmapFactory.decodeStream(is);
					imageCacheManager.putImageCache(cid, b, size);
				} else if (resultCode == HttpStatus.SC_UNAUTHORIZED
						|| resultCode == HttpStatus.SC_NOT_FOUND) {
					HttpEntity httpEntity = response.getEntity();
					InputStream is = httpEntity.getContent();
					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(is));
					String line = "";
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

					Log.d("http image err", sb.toString());
				}
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
		}

		return b;
	}

	@Override
	public void callback() {
		if (mOnImageLoadListener != null) {
			mOnImageLoadListener.onImageLoaded(mTaskCode, mCid, mSize, mResult);
		}
	}

	public static interface OnImageLoadListener {
		public void onImageLoaded(int taskCode, String cid, String size,
				Bitmap image);
	}

}
