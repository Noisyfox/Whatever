package mynuaa.whatever;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MaskedImageView extends ImageView {

	private int mImageSource = 0;
	private int mMaskSource = 0;
	private int mStampSource = 0;

	private Bitmap mImage = null, mMask = null, mStamp = null;

	public MaskedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.MaskedImageView);

		mImageSource = typedArray.getResourceId(
				R.styleable.MaskedImageView_image, 0);
		mMaskSource = typedArray.getResourceId(
				R.styleable.MaskedImageView_mask, 0);
		mStampSource = typedArray.getResourceId(
				R.styleable.MaskedImageView_stamp, 0);

		RuntimeException exception = null;
		if (mMaskSource == 0) {
			exception = new IllegalArgumentException(
					typedArray.getIndexCount()
							+ ": The content attribute is required and must refer to a valid image.");
		}
		typedArray.recycle();
		if (exception != null)
			throw exception;

		// 获取图片的资源文件
		mImage = mImageSource == 0 ? null : BitmapFactory.decodeResource(
				getResources(), mImageSource);
		// 获取遮罩层图片
		mMask = BitmapFactory.decodeResource(getResources(), mMaskSource);
		// 获取图章图片
		mStamp = mStampSource == 0 ? null : BitmapFactory.decodeResource(
				getResources(), mStampSource);

		setIconBitmap(mImage);
	}

	private Bitmap getBlendBitmap(Bitmap original, Bitmap mask, Bitmap stamp) {
		Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
				Config.ARGB_8888);
		Canvas mCanvas = new Canvas(result);

		if (original != null) {
			// 将遮罩层的图片放到画布中
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			// 设置两张图片相交时的模式
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

			// 填充图像
			double ra_orig = (double) original.getWidth()
					/ (double) original.getHeight();
			double ra_mask = (double) mask.getWidth()
					/ (double) mask.getHeight();
			Rect src;
			Rect dst = new Rect(0, 0, mask.getWidth(), mask.getHeight());
			double k, x, y, w, h;
			if (ra_orig > ra_mask) { // 原图更宽，按高
				k = (double) original.getHeight() / (double) mask.getHeight();
				y = 0;
				h = original.getHeight();
				w = (double) mask.getWidth() * k;
				x = (original.getWidth() - w) / 2D;
			} else { // 遮罩更宽，按宽
				k = (double) original.getWidth() / (double) mask.getWidth();
				x = 0;
				w = original.getWidth();
				h = (double) mask.getHeight() * k;
				y = (original.getHeight() - h) / 2D;
			}
			src = new Rect((int) x, (int) y, (int) (x + w), (int) (y + h));
			mCanvas.drawBitmap(original, src, dst, null);

			mCanvas.drawBitmap(mask, 0, 0, paint);
			paint.setXfermode(null);
		}
		if (stamp != null) {
			mCanvas.drawBitmap(stamp, 0, 0, null);
		}

		return result;
	}

	public void setIconId(int id) {
		mImageSource = id;
		mImage = mImageSource <= 0 ? null : BitmapFactory.decodeResource(
				getResources(), mImageSource);

		Bitmap result = getBlendBitmap(mImage, mMask, mStamp);
		setImageBitmap(result);
		setScaleType(ScaleType.CENTER);
	}

	public void setStampId(int id) {
		mStampSource = id;
		mStamp = mStampSource <= 0 ? null : BitmapFactory.decodeResource(
				getResources(), mStampSource);

		Bitmap result = getBlendBitmap(mImage, mMask, mStamp);
		setImageBitmap(result);
		setScaleType(ScaleType.CENTER);
	}

	public void setIconBitmap(Bitmap bitmap) {
		mImage = bitmap;

		Bitmap result = getBlendBitmap(mImage, mMask, mStamp);
		setImageBitmap(result);
		setScaleType(ScaleType.CENTER);
	}

	public void setStampBitmap(Bitmap bitmap) {
		mStamp = bitmap;

		Bitmap result = getBlendBitmap(mImage, mMask, mStamp);
		setImageBitmap(result);
		setScaleType(ScaleType.CENTER);
	}

	public Bitmap getIconBitmap() {
		return mImage;
	}

	public Bitmap getStampBitmap() {
		return mStamp;
	}

	public Bitmap getMaskBitmap() {
		return mMask;
	}
}
