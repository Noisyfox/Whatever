package mynuaa.whatever;

import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends SherlockActivity implements
		OnClickListener {

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_out_right);
	}

	public static void startWebView(Activity activity, String uri) {
		Intent i = new Intent();
		i.setClass(activity, WebViewActivity.class);
		i.putExtra("uri", uri);

		activity.startActivity(i);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
	}

	private String uri;
	private WebView webView;
	private View btn_prev, btn_next, btn_refresh;
	private ProgressBar progressBar_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);

		uri = getIntent().getStringExtra("uri");

		if (uri == null) {
			finish();
			return;
		}

		webView = (WebView) findViewById(R.id.webView);
		btn_prev = findViewById(R.id.btn_prev);
		btn_next = findViewById(R.id.btn_next);
		btn_refresh = findViewById(R.id.btn_refresh);
		progressBar_loading = (ProgressBar) findViewById(R.id.progressBar_loading);

		btn_prev.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_refresh.setOnClickListener(this);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.requestFocus();

		webView.loadUrl(uri);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				progressBar_loading.setProgress(progress);
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 设置点击网页里面的链接还是在当前的webview里跳转
				view.loadUrl(url);
				return true;
			}
		});

	}

	@Override
	public void onClick(View v) {
		if (v == btn_prev) {
			webView.goBack();
		} else if (v == btn_next) {
			webView.goForward();
		} else if (v == btn_refresh) {
			webView.reload();
		}
	}
}
