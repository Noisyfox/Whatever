package mynuaa.whatever;

import java.util.Random;

import mynuaa.whatever.DataSource.DataCenter;
import mynuaa.whatever.DataSource.MessageData;
import mynuaa.whatever.DataSource.MessagePostTask;
import mynuaa.whatever.DataSource.MessagePostTask.OnMessagePostListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WriteMessageActivity extends SherlockFragmentActivity implements
		TextWatcher, OnClickListener, OnMessagePostListener,
		EmojiconGridFragment.OnEmojiconClickedListener,
		EmojiconsFragment.OnEmojiconBackspaceClickedListener {
	private static final String TASK_TAG = "task_write_message_activity";

	private static final int REQUEST_GETIMG = 1;

	private static Bitmap background_colors_bitmap_cache[] = new Bitmap[MessageTheme.background_colors.length];
	private static Bitmap background_texture_bitmap_cache[] = new Bitmap[MessageTheme.background_textures.length];
	private static Bitmap addimage_bitmap_cache[] = new Bitmap[MessageTheme.background_colors.length];
	private static BitmapDrawable background_addimage_bitmap_cache[] = new BitmapDrawable[MessageTheme.background_colors.length];
	private static Random mRandom = new Random();

	private boolean isEdited = false;
	private boolean imageAdded = false;
	private Bitmap imageBitmap = null;

	private int selectedColor = -1;
	private int selectedTexture = 0;
	private Bitmap cachedBackground = null;

	private Fragment fragment_emoji;

	private ImageView imageView_background;
	private EditText editText_message;
	private TextView textView_addImage;
	private View layout_addImage;
	private ImageView imageView_addImage;
	private Button btn_send;
	private ImageView imageView_image;
	private View button_emoji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write_message);

		imageView_background = (ImageView) findViewById(R.id.imageView_background);
		editText_message = (EditText) findViewById(R.id.editText_message);
		textView_addImage = (TextView) findViewById(R.id.textView_addImage);
		layout_addImage = findViewById(R.id.linearLayout_addImage);
		imageView_addImage = (ImageView) findViewById(R.id.imageView_addImage);
		imageView_image = (ImageView) findViewById(R.id.imageView_image);
		button_emoji = findViewById(R.id.button_emoji);

		FragmentManager fm = getSupportFragmentManager();
		fragment_emoji = fm.findFragmentById(R.id.fragment_emoji);

		button_emoji.setOnClickListener(this);

		Util.setupCommonActionBar(this, R.string.write_message_title);
		btn_send = (Button) getSupportActionBar().getCustomView().findViewById(
				R.id.right_btn);
		btn_send.setVisibility(View.VISIBLE);
		btn_send.setText(R.string.btn_write_message_sned);
		btn_send.setOnClickListener(this);
		btn_send.setEnabled(false);

		final HListView hv_color = (HListView) findViewById(R.id.listView_color);
		final HListView hv_texture = (HListView) findViewById(R.id.listView_texture);

		hv_color.setAdapter(new ColorSelectorAdapter());
		hv_texture.setAdapter(new TextureSelectorAdapter());

		hv_color.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				applyTheme(arg2, selectedTexture);
				isEdited = true;
			}
		});

		hv_texture.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				applyTheme(selectedColor, arg2);
				isEdited = true;
			}
		});

		applyTheme(mRandom.nextInt(MessageTheme.background_colors.length), 0);// 随机选取一套颜色

		editText_message.addTextChangedListener(this);
		Util.hideHintOnFocused(editText_message);
		editText_message.setOnClickListener(this);

		layout_addImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, REQUEST_GETIMG);
			}
		});
		imageView_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isEdited = true;
				Dialog alertDialog = new MyAlertDialog.Builder(
						WriteMessageActivity.this)
						.setMessage("确定移除这张图片吗？")
						.setPositiveButton("确定",
								new MyAlertDialog.OnClickListener() {
									@Override
									public boolean onClick(
											DialogInterface dialog, int which) {
										putImage(false);

										return true;
									}
								}).setNegativeButton("取消", null).create();
				alertDialog.show();
			}
		});

		putImage(false);

		DataCenter.startLoactionService();
		DataCenter.requireLocation();

		WhateverApplication.getMainTaskManager().activateTag(TASK_TAG);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		FragmentManager fm = getSupportFragmentManager();
		Util.toggleEmojiFragment(fm, fragment_emoji, false);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void putImage(boolean addPic) {
		imageAdded = addPic;

		if (imageAdded) {
			layout_addImage.setVisibility(View.GONE);
			imageView_image.setVisibility(View.VISIBLE);

			Bitmap preview = Util.creatPreviewImage(imageBitmap);

			imageView_image.setImageBitmap(preview);
		} else {
			layout_addImage.setVisibility(View.VISIBLE);
			imageView_image.setVisibility(View.GONE);

			imageBitmap = null;
			imageView_image.setImageBitmap(null);
		}
	}

	private void askForCancel() {
		if (!isEdited) {
			finishProxy();
		} else {
			Dialog alertDialog = new MyAlertDialog.Builder(this)
					.setMessage("放弃发布这条状态吗？")
					.setPositiveButton("确定",
							new MyAlertDialog.OnClickListener() {
								@Override
								public boolean onClick(DialogInterface dialog,
										int which) {
									finishProxy();

									return true;
								}
							}).setNegativeButton("取消", null).create();
			alertDialog.show();
		}
	}

	private synchronized void finishProxy() {
		super.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_out_bottom);
	}

	@Override
	public void finish() {
		askForCancel();
	}

	@Override
	protected void onDestroy() {
		putImage(false);
		WhateverApplication.getMainTaskManager().deactivateTag(TASK_TAG);

		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	private void applyTheme(int colorI, int textureI) {
		int color_message = MessageTheme.background_colors[colorI][1];

		if (selectedColor != colorI || selectedTexture != textureI) {
			cachedBackground = MessageTheme.createBackground(
					WriteMessageActivity.this, colorI, textureI);
		}
		imageView_background.setImageBitmap(cachedBackground);

		editText_message.setTextColor(color_message);
		editText_message.setHintTextColor(color_message);

		textView_addImage.setTextColor(color_message);

		if (background_addimage_bitmap_cache[colorI] == null) {
			Bitmap mask = BitmapFactory.decodeResource(getResources(),
					R.drawable.add_pic);
			Bitmap result = MessageTheme.colorOverlay(mask, color_message);

			background_addimage_bitmap_cache[colorI] = new BitmapDrawable(
					getResources(), result);
		}

		if (addimage_bitmap_cache[colorI] == null) {
			Bitmap mask = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_action_new);

			addimage_bitmap_cache[colorI] = MessageTheme.colorOverlay(mask,
					color_message);
		}

		layout_addImage
				.setBackgroundDrawable(background_addimage_bitmap_cache[colorI]);
		imageView_addImage.setImageBitmap(addimage_bitmap_cache[colorI]);

		selectedColor = colorI;
		selectedTexture = textureI;

	}

	class ColorSelectorAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return MessageTheme.background_colors.length;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			if (convertView == null)
				convertView = LayoutInflater.from(WriteMessageActivity.this)
						.inflate(R.layout.write_message_select_button, arg2,
								false);

			MaskedImageView miv = (MaskedImageView) convertView
					.findViewById(R.id.btn_selector);

			if (background_colors_bitmap_cache[arg0] == null) {
				Bitmap result = Bitmap.createBitmap(miv.getMaskBitmap()
						.getWidth(), miv.getMaskBitmap().getHeight(),
						Config.ARGB_8888);
				Canvas mCanvas = new Canvas(result);
				mCanvas.drawColor(MessageTheme.background_colors[arg0][0]);

				if (MessageTheme.background_colors[arg0][0] == 0xffffffff) {// 白色的边框
					Bitmap white_stamp = BitmapFactory.decodeResource(
							getResources(), R.drawable.selector_stamp_white);
					mCanvas.drawBitmap(white_stamp, 0, 0, null);
				}

				background_colors_bitmap_cache[arg0] = result;
			}

			miv.setIconBitmap(background_colors_bitmap_cache[arg0]);

			return convertView;
		}
	}

	class TextureSelectorAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return MessageTheme.background_textures.length;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			if (convertView == null)
				convertView = LayoutInflater.from(WriteMessageActivity.this)
						.inflate(R.layout.write_message_select_button, arg2,
								false);

			MaskedImageView miv = (MaskedImageView) convertView
					.findViewById(R.id.btn_selector);

			if (background_texture_bitmap_cache[arg0] == null) {
				Bitmap result = Bitmap.createBitmap(miv.getMaskBitmap()
						.getWidth(), miv.getMaskBitmap().getHeight(),
						Config.ARGB_8888);
				Canvas mCanvas = new Canvas(result);
				mCanvas.drawColor(0xFFFFFFFF);
				mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
						MessageTheme.background_textures[arg0][0][0]), 0, 0,
						null);

				background_texture_bitmap_cache[arg0] = result;
			}

			miv.setIconBitmap(background_texture_bitmap_cache[arg0]);

			return convertView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_GETIMG && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			imageBitmap = BitmapFactory.decodeFile(picturePath);

			if (imageBitmap == null) {
				Toast.makeText(WriteMessageActivity.this, "载入图片失败",
						Toast.LENGTH_SHORT).show();
			} else {
				putImage(true);
				isEdited = true;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void afterTextChanged(Editable s) {
		btn_send.setEnabled(s.length() > 0);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		isEdited = true;
	}

	private ProgressDialog mPostDialog = null;

	@Override
	public void onClick(View arg0) {
		if (arg0 == btn_send) {
			String message = editText_message.getText().toString();
			if (Util.isBlank(message)) {
				Toast.makeText(this, "不可以什么都不说哦~", Toast.LENGTH_SHORT).show();
				return;
			}
			mPostDialog = ProgressDialog.show(this, "发布状态中", "请稍候", false);

			MessageData md = new MessageData();

			md.content = editText_message.getText().toString();
			md.background_color_index = this.selectedColor;
			md.background_texture_index = this.selectedTexture;
			md.image = imageBitmap;
			md.image_cid = "";

			WhateverApplication.getMainTaskManager().startTask(
					new MessagePostTask(TASK_TAG, md, this));
		} else if (arg0 == button_emoji) {
			Util.setInputMethod(this, false);

			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, true);
		} else if (arg0 == editText_message) {
			FragmentManager fm = getSupportFragmentManager();
			Util.toggleEmojiFragment(fm, fragment_emoji, false);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (fragment_emoji.isVisible()) {
				FragmentManager fm = getSupportFragmentManager();
				Util.toggleEmojiFragment(fm, fragment_emoji, false);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onMessagePost(int result, MessageData message) {
		if (mPostDialog != null) {
			mPostDialog.dismiss();
		}

		if (result == MessagePostTask.POST_SUCCESS) {
			Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
			finishProxy();
		} else if (result == MessagePostTask.POST_FAIL_IMAGE_UPLOAD_FAIL) {
			Toast.makeText(this, "发布失败：未能上传图片", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "发布失败", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(editText_message);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(editText_message, emojicon);
	}

}
