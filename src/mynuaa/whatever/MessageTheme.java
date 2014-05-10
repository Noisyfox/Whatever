package mynuaa.whatever;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;

public class MessageTheme {

	public static final int background_colors[][] = {
			{ 0xffffffff, 0xff000000, 0xffaeaeae, 1 },
			{ 0xff000000, 0xffb6b4b4, 0xff000000, 0 },
			{ 0xff7a2127, 0xffffffff, 0xff7a2127, 0 },
			{ 0xffd54642, 0xffffffff, 0xffd54642, 0 },
			{ 0xffa44b3b, 0xffffffff, 0xffa44b3b, 0 },
			{ 0xff804d3b, 0xffffffff, 0xff804d3b, 0 },
			{ 0xfff6b752, 0xffffffff, 0xfff6b752, 0 },
			{ 0xffe1cd96, 0xff4f4e4e, 0xffe1cd96, 0 },
			{ 0xffa9cb6a, 0xffffffff, 0xffa9cb6a, 0 },
			{ 0xff488f63, 0xffffffff, 0xff488f63, 0 },
			{ 0xff27ae61, 0xffffffff, 0xff27ae61, 0 },
			{ 0xff627523, 0xffffffff, 0xff627523, 0 },
			{ 0xff407d6e, 0xffffffff, 0xff407d6e, 0 },
			{ 0xffb1d6dc, 0xff1c4046, 0xffb1d6dc, 0 },
			{ 0xff16a086, 0xffffffff, 0xff16a086, 0 },
			{ 0xff28c1bc, 0xffffffff, 0xff28c1bc, 0 },
			{ 0xff297fb8, 0xffffffff, 0xff297fb8, 0 },
			{ 0xff5a9498, 0xffffffff, 0xff5a9498, 0 },
			{ 0xff576474, 0xffffffff, 0xff576474, 0 },
			{ 0xff3f5057, 0xffffffff, 0xff3f5057, 0 },
			{ 0xff907b76, 0xffffffff, 0xff907b76, 0 },
			{ 0xff784e5a, 0xffffffff, 0xff784e5a, 0 },
			{ 0xffa58abd, 0xffffffff, 0xffa58abd, 0 },
			{ 0xffad5e97, 0xffffffff, 0xffad5e97, 0 },
			{ 0xff9a59b5, 0xffffffff, 0xff9a59b5, 0 },
			{ 0xff584562, 0xffffffff, 0xff584562, 0 },
			{ 0xff3e215b, 0xffffffff, 0xff3e215b, 0 } };

	public static final int COLOR_BACKGROUND = 0;
	public static final int COLOR_TEXT = 1;
	public static final int COLOR_CORNER = 2;

	public static final int getColor(int theme, int colorType) {
		return background_colors[theme][colorType];
	}

	public static final int TEXTURE_NONE = 1; // 无效果
	public static final int TEXTURE_MULTIPLY = 2; // 正片叠底
	public static final int TEXTURE_PUT = 3; // 放置
	private static final Paint p_normal = new Paint(Paint.ANTI_ALIAS_FLAG);

	public static final int background_textures[][][] = {
			{
					{ R.drawable.texure_r_0, R.drawable.texure_0, TEXTURE_NONE },
					{ R.drawable.texure_r_0, R.drawable.texure_0, TEXTURE_NONE } },
			{
					{ R.drawable.texure_r_1, R.drawable.texure_muti_1,
							TEXTURE_MULTIPLY },
					{ R.drawable.texure_r_1, R.drawable.texure_muti_1,
							TEXTURE_MULTIPLY } },
			{
					{ R.drawable.texure_r_2, R.drawable.texure_muti_2,
							TEXTURE_MULTIPLY },
					{ R.drawable.texure_r_2, R.drawable.texure_muti_2,
							TEXTURE_MULTIPLY } },
			{
					{ R.drawable.texure_r_3, R.drawable.texure_muti_3,
							TEXTURE_MULTIPLY },
					{ R.drawable.texure_r_3, R.drawable.texure_muti_3,
							TEXTURE_MULTIPLY } },
			{
					{ R.drawable.texure_r_4, R.drawable.texure_muti_4,
							TEXTURE_MULTIPLY },
					{ R.drawable.texure_r_4, R.drawable.texure_muti_4,
							TEXTURE_MULTIPLY } },
			{
					{ R.drawable.texure_r_5, R.drawable.texure_muti_5,
							TEXTURE_MULTIPLY },
					{ R.drawable.texure_r_5, R.drawable.texure_muti_5,
							TEXTURE_MULTIPLY } },
			{
					{ R.drawable.texure_r_6, R.drawable.texure_muti_6,
							TEXTURE_MULTIPLY },
					{ R.drawable.texure_r_6, R.drawable.texure_muti_6,
							TEXTURE_MULTIPLY } },
			{
					{ R.drawable.texure_r_7, R.drawable.texure_put_1,
							TEXTURE_PUT },
					{ R.drawable.texure_r_7, R.drawable.texure_put_1,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_8, R.drawable.texure_put_2,
							TEXTURE_PUT },
					{ R.drawable.texure_r_8, R.drawable.texure_put_2,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_9, R.drawable.texure_put_3,
							TEXTURE_PUT },
					{ R.drawable.texure_r_9, R.drawable.texure_put_3,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_10, R.drawable.texure_put_4,
							TEXTURE_PUT },
					{ R.drawable.texure_r_10, R.drawable.texure_put_4,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_11, R.drawable.texure_put_5,
							TEXTURE_PUT },
					{ R.drawable.texure_r_11, R.drawable.texure_put_5,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_12, R.drawable.texure_put_6,
							TEXTURE_PUT },
					{ R.drawable.texure_r_12, R.drawable.texure_put_6,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_13, R.drawable.texure_put_7,
							TEXTURE_PUT },
					{ R.drawable.texure_r_13, R.drawable.texure_put_7,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_14, R.drawable.texure_put_8,
							TEXTURE_PUT },
					{ R.drawable.texure_r_14, R.drawable.texure_put_8,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_15, R.drawable.texure_put_9,
							TEXTURE_PUT },
					{ R.drawable.texure_r_15, R.drawable.texure_put_9,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_16, R.drawable.texure_put_10,
							TEXTURE_PUT },
					{ R.drawable.texure_r_16, R.drawable.texure_put_10,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_17, R.drawable.texure_put_11,
							TEXTURE_PUT },
					{ R.drawable.texure_r_17, R.drawable.texure_put_11,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_18, R.drawable.texure_put_12,
							TEXTURE_PUT },
					{ R.drawable.texure_r_18, R.drawable.texure_put_12,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_19, R.drawable.texure_put_13,
							TEXTURE_PUT },
					{ R.drawable.texure_r_19, R.drawable.texure_put_13,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_20, R.drawable.texure_put_14,
							TEXTURE_PUT },
					{ R.drawable.texure_r_20, R.drawable.texure_put_14,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_21, R.drawable.texure_put_15,
							TEXTURE_PUT },
					{ R.drawable.texure_r_21, R.drawable.texure_put_15,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_22, R.drawable.texure_put_16,
							TEXTURE_PUT },
					{ R.drawable.texure_r_22, R.drawable.texure_put_16,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_23, R.drawable.texure_put_17,
							TEXTURE_PUT },
					{ R.drawable.texure_r_23, R.drawable.texure_put_17,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_24, R.drawable.texure_put_18,
							TEXTURE_PUT },
					{ R.drawable.texure_r_24, R.drawable.texure_put_18,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_25, R.drawable.texure_put_19,
							TEXTURE_PUT },
					{ R.drawable.texure_r_25, R.drawable.texure_put_19,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_26, R.drawable.texure_put_20,
							TEXTURE_PUT },
					{ R.drawable.texure_r_26, R.drawable.texure_put_20,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_27, R.drawable.texure_put_21,
							TEXTURE_PUT },
					{ R.drawable.texure_r_27, R.drawable.texure_put_21,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_28, R.drawable.texure_put_22,
							TEXTURE_PUT },
					{ R.drawable.texure_r_28, R.drawable.texure_put_22,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_29, R.drawable.texure_put_23,
							TEXTURE_PUT },
					{ R.drawable.texure_r_29, R.drawable.texure_put_23,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_30, R.drawable.texure_put_24,
							TEXTURE_PUT },
					{ R.drawable.texure_r_30, R.drawable.texure_put_24,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_31, R.drawable.texure_put_25,
							TEXTURE_PUT },
					{ R.drawable.texure_r_31, R.drawable.texure_put_25,
							TEXTURE_PUT } },
			{
					{ R.drawable.texure_r_32, R.drawable.texure_put_26,
							TEXTURE_PUT },
					{ R.drawable.texure_r_32, R.drawable.texure_put_26,
							TEXTURE_PUT } } };

	public static Bitmap createBackground(Context context, int colorId,
			int textureId) {
		int color_background = background_colors[colorId][0];
		int texture_s = background_colors[colorId][3];
		Bitmap bkg = null;

		switch (background_textures[textureId][texture_s][2]) {
		case TEXTURE_NONE: {
			bkg = Bitmap.createBitmap(new int[] { color_background }, 1, 1,
					Config.ARGB_8888);
			break;
		}
		case TEXTURE_MULTIPLY: {
			Bitmap texture = BitmapFactory.decodeResource(
					context.getResources(),
					background_textures[textureId][texture_s][1]);
			int w = texture.getWidth(), h = texture.getHeight();
			int pixels[] = new int[w * h];
			texture.getPixels(pixels, 0, w, 0, 0, w, h);

			int c_r = ((color_background >> 16) & 0xFF), c_g = ((color_background >> 8) & 0xFF), c_b = (color_background & 0xFF);

			// 正片叠底
			for (int i = 0; i < pixels.length; i++) {
				int t_r = ((pixels[i] >> 16) & 0xFF), t_g = ((pixels[i] >> 8) & 0xFF), t_b = (pixels[i] & 0xFF);
				int r_r = c_r * t_r / 255, r_g = c_g * t_g / 255, r_b = c_b
						* t_b / 255;

				pixels[i] = 0xFF00;
				pixels[i] |= (r_r & 0xFF);
				pixels[i] <<= 8;
				pixels[i] |= (r_g & 0xFF);
				pixels[i] <<= 8;
				pixels[i] |= (r_b & 0xFF);
			}
			bkg = Bitmap.createBitmap(pixels, w, h, Config.ARGB_8888);
			break;
		}
		case TEXTURE_PUT: {
			Bitmap texture = BitmapFactory.decodeResource(
					context.getResources(),
					background_textures[textureId][texture_s][1]);
			bkg = Bitmap.createBitmap(texture.getWidth(), texture.getHeight(),
					Config.ARGB_8888);
			Canvas c = new Canvas(bkg);
			c.drawColor(color_background);
			c.drawBitmap(texture, 0, 0, p_normal);
			break;
		}
		}

		return bkg;
	}

	/**
	 * 颜色叠加
	 * 
	 * @param res
	 * @param color
	 * @return
	 */
	public static Bitmap colorOverlay(Bitmap res, int color) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// 设置两张图片相交时的模式
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		Bitmap result = Bitmap.createBitmap(res.getWidth(), res.getHeight(),
				Config.ARGB_8888);
		Canvas mCanvas = new Canvas(result);

		mCanvas.drawColor(color);
		mCanvas.drawBitmap(res, 0, 0, paint);

		return result;
	}
}
