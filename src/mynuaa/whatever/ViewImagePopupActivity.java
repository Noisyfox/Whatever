package mynuaa.whatever;

import mynuaa.whatever.DataSource.ImageLoadTask;
import mynuaa.whatever.DataSource.ImageLoadTask.OnImageLoadListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.graphics.Bitmap;

public class ViewImagePopupActivity extends Activity implements
		OnImageLoadListener, OnClickListener {
	private static final String TASK_TAG = "task_viewimage_activity";

	private String image_cid = null;

	private TouchImageView imageView_image;
	private View view_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image_popup);
		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);

		imageView_image = (TouchImageView) findViewById(R.id.imageView_image);
		view_loading = findViewById(R.id.progressBar_loading);

		imageView_image.setOnClickListener(this);

		image_cid = this.getIntent().getStringExtra("image_cid");

		image_cid = image_cid == null ? "" : image_cid;

		if (!image_cid.isEmpty()) {
			WhateverApplication.getMainTaskManager().startTask(
					new ImageLoadTask(TASK_TAG, 0, image_cid, "big", this));
		}
	}

	@Override
	protected void onDestroy() {
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);
		super.onDestroy();
	}

	@Override
	public void onImageLoaded(int taskCode, String cid, String size,
			Bitmap image) {
		if (image_cid.equals(cid)) {
			view_loading.setVisibility(View.GONE);
			if (image == null) { // Ê§°Ü
				imageView_image
						.setImageResource(R.drawable.image_broken_n_dark);
			} else {
				imageView_image.setImageBitmap(image);
			}
		}
	}

	@Override
	public void onClick(View v) {
		finish();
	}

}
