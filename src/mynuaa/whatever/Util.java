package mynuaa.whatever;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.rockerhieu.emojicon.EmojiconHandler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Util {

	private static final OnFocusChangeListener hintHideListener = new OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			EditText _v = (EditText) v;
			if (!hasFocus) {// 失去焦点
				_v.setHint(_v.getTag().toString());
			} else {
				String hint = _v.getHint().toString();
				_v.setTag(hint);
				_v.setHint("");
			}
		}
	};

	public static void hideHintOnFocused(EditText et) {
		et.setOnFocusChangeListener(hintHideListener);
	}

	private static final InputFilter[] IF_NOINPUT = new InputFilter[] { new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			return null;
		}
	} };
	private static final InputFilter[] IF_FREE = new InputFilter[] {};

	public static void setEditable(EditText et, boolean editable) {
		et.setFocusable(editable);
		et.setFocusableInTouchMode(editable);
		et.setCursorVisible(editable);

		et.setFilters(editable ? IF_FREE : IF_NOINPUT);
	}

	public static void setInputMethod(Activity activity, boolean show) {

		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (show) {
			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
		} else {
			// 隐藏输入法
			View currentFocused = activity.getCurrentFocus();
			if (currentFocused != null) {
				imm.hideSoftInputFromWindow(currentFocused.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	public static void toggleEmojiFragment(FragmentManager fm, Fragment ef,
			boolean show) {
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
		if (show) {
			ft.show(ef);
		} else {
			ft.hide(ef);
		}
		ft.commit();
	}

	public static Bitmap creatPreviewImage(Bitmap res) {
		int nh = (int) (res.getHeight() * (512.0 / res.getWidth()));
		Bitmap scaled = Bitmap.createScaledBitmap(res, 512, nh, true);

		return scaled;
	}

	public static double[] generateGaussianMatrx(int radius) {
		if (radius < 1) {
			throw new IllegalArgumentException();
		}
		double s = radius, s2 = s * s;
		double k = 1.0 / (2.0 * Math.PI * s2);
		double k2 = -1.0 / (2.0 * s2);
		double matrx[] = new double[(radius * 2 + 1) * (radius * 2 + 1)];

		int l = 0;
		// double sum = 0;
		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				matrx[l] = Math.exp((double) (x * x + y * y) * k2) * k;
				// sum += matrx[l];
				l++;
			}
		}

		// for (int i = 0; i < matrx.length; i++) {
		// matrx[i] /= sum;
		// }

		return matrx;
	}

	public static Bitmap doGaussianBlur(Bitmap src, int radius, double[] matrx) {
		if (matrx == null) {
			matrx = generateGaussianMatrx(radius);
		}

		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int[] pixels = new int[width * height];
		int[] npixels = new int[width * height];
		src.getPixels(pixels, 0, width, 0, 0, width, height);

		int p = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double na = 0, nr = 0, ng = 0, nb = 0;
				double sum = 0;
				int gl = 0;
				for (int dy = -radius; dy <= radius; dy++) {
					for (int dx = -radius; dx <= radius; dx++) {
						int rx = x + dx, ry = y + dy;
						if (rx >= 0 && rx < width && ry >= 0 && ry < height) {
							sum += matrx[gl];
							int rl = rx + ry * width;
							int a = Color.alpha(pixels[rl]), r = Color
									.red(pixels[rl]), g = Color
									.green(pixels[rl]), b = Color
									.blue(pixels[rl]);
							na += a * matrx[gl];
							nr += r * matrx[gl];
							ng += g * matrx[gl];
							nb += b * matrx[gl];
						}
						gl++;
					}
				}
				na /= sum;
				nr /= sum;
				ng /= sum;
				nb /= sum;

				npixels[p] = Color.argb((int) na, (int) nr, (int) ng, (int) nb);

				p++;
			}
		}

		bitmap.setPixels(npixels, 0, width, 0, 0, width, height);

		return bitmap;
	}

	private static double MATRIX_HEAD[] = null;
	private static int RADIUS_HEAD = 10;
	private static Paint PAINT_HEAD = null;

	public static Bitmap blurHeadBackground(Bitmap src) {
		if (MATRIX_HEAD == null) {
			MATRIX_HEAD = generateGaussianMatrx(RADIUS_HEAD);
		}
		if (PAINT_HEAD == null) {
			PAINT_HEAD = new Paint();
			PAINT_HEAD.setAlpha(99);
		}

		Bitmap blur = doGaussianBlur(src, RADIUS_HEAD, MATRIX_HEAD);

		Bitmap result = Bitmap.createBitmap(blur.getWidth(), blur.getHeight(),
				Config.ARGB_8888);
		Canvas c = new Canvas(result);
		c.drawColor(Color.WHITE);
		c.drawBitmap(blur, 0, 0, PAINT_HEAD);

		return result;
	}

	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER };
	/** 联系人显示名称 **/
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;

	/** 电话号码 **/
	private static final int PHONES_NUMBER_INDEX = 1;

	public static List<String> getContactAllPhoneNumber(Context context) {
		ArrayList<String> phoneList = new ArrayList<String>();
		ContentResolver resolver = context.getContentResolver();

		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);

		if (phoneCursor != null && phoneCursor.moveToFirst()) {
			do {
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				if (!TextUtils.isEmpty(phoneNumber)) {
					phoneList.add(regularPhoneNumber(phoneNumber));
				}
			} while (phoneCursor.moveToNext());
		}
		phoneCursor.close();

		return phoneList;
	}

	public static String findContactNameByNumber(Context context, String number) {
		String rn = regularPhoneNumber(number);
		ContentResolver resolver = context.getContentResolver();

		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);

		String dname = null;
		if (phoneCursor != null && phoneCursor.moveToFirst()) {
			do {
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				String name = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
				if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phoneNumber)
						&& rn.equals(regularPhoneNumber(phoneNumber))) {
					dname = name;
					break;
				}
			} while (phoneCursor.moveToNext());
		}
		phoneCursor.close();

		return dname;
	}

	public static String regularPhoneNumber(String rawNumber) {
		char rawN[] = rawNumber.toCharArray();
		StringBuilder sb = new StringBuilder(20);

		for (int i = 0; i < rawN.length; i++) {
			if (rawN[i] == '+') {// 只考虑+86这个前缀长度为3的情况
				i += 2;
			} else if (Character.isDigit(rawN[i])) {
				sb.append(rawN[i]);
			}
		}

		return sb.toString();
	}

	public static void set_button_Drawable_center(final Activity activity,
			final Handler handler, final Button button, final int imageID,
			final int spacing) {

		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (button.getMeasuredWidth() == 0) {
					handler.postDelayed(this, 0);
				} else {
					Drawable drawable = activity.getResources().getDrawable(
							imageID);
					int width = button.getMeasuredWidth();
					// int height = button.getMeasuredHeight();

					Rect bounds = new Rect();
					Paint textPaint = button.getPaint();
					textPaint.getTextBounds(button.getText().toString(), 0,
							button.getText().length(), bounds);
					// int txt_height = bounds.height();
					int txt_width = bounds.width();

					int img_width = drawable.getIntrinsicWidth();
					// int img_height = drawable.getIntrinsicHeight();
					// int content_height = txt_height + img_height + spacing;
					int content_width = txt_width + img_width + spacing;

					int padding_w = 0;
					if (width >= content_width)
						padding_w = width / 2 - content_width / 2;
					// int padding_h = height / 2 - content_height / 2;

					button.setCompoundDrawablesWithIntrinsicBounds(drawable,
							null, null, null);
					button.setPadding(padding_w, button.getPaddingTop(), 0,
							button.getPaddingBottom());
					button.setCompoundDrawablePadding(-padding_w);
				}
			}
		};
		handler.postDelayed(runnable, 0);
	}

	private static final DateFormat DF_orig = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private static final DateFormat DF_target = new SimpleDateFormat(
			"MM-dd HH:mm", Locale.CHINA);

	public static String formatTime(long time) {
		return DF_target.format(new Date(time));
	}

	public static String formatTime(String origin) {
		try {
			return DF_target.format(DF_orig.parse(origin));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return origin;
	}

	public static long parseServerTime(String time) {
		try {
			return DF_orig.parse(time).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static String messageDecode(String orig) {
		orig = orig.replace("\\\\", "\\");
		String str = "[\"" + orig + "\"]";
		try {
			JSONArray ja = new JSONArray(str);
			str = ja.getString(0);
			return str;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String r = orig.replace("\\n", "\n");

		return r;
	}

	public static String messageEncode(String orig) {
		try {
			return emojiEncode(orig);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Oops! Something goes wrong! But luckily we have a fallback here!
		StringBuilder sb = new StringBuilder();
		char chars[] = orig.toCharArray();
		for (char c : chars) {
			sb.append(String.format("\\u%04x", (int) c));
		}
		return sb.toString();
	}

	// Some copy code from com.rockerhieu.emojicon.EmojiconHandler
	private static String emojiEncode(String orig) {
		StringBuilder sb = new StringBuilder();

		char chars[] = orig.toCharArray();

		int skip = 0;
		for (int i = 0; i < chars.length; i += skip) {
			int icon = 0;

			char c = chars[i];
			if (EmojiconHandler.isSoftBankEmoji(c)) {
				icon = EmojiconHandler.getSoftbankEmojiResource(c);
				skip = icon == 0 ? 0 : 1;
			}

			if (icon == 0) {
				int unicode = Character.codePointAt(chars, i);
				skip = Character.charCount(unicode);

				if (unicode > 0xff) {
					icon = EmojiconHandler.getEmojiResource(null, unicode);
				}

				if (icon == 0 && i + skip < chars.length) {
					int followUnicode = Character.codePointAt(chars, i + skip);
					if (followUnicode == 0x20e3) {
						int followSkip = Character.charCount(followUnicode);
						switch (unicode) {
						case 0x0031:
						case 0x0032:
						case 0x0033:
						case 0x0034:
						case 0x0035:
						case 0x0036:
						case 0x0037:
						case 0x0038:
						case 0x0039:
						case 0x0030:
						case 0x0023:
							icon = 1;
							break;
						default:
							followSkip = 0;
							break;
						}
						skip += followSkip;
					} else {
						int followSkip = Character.charCount(followUnicode);
						switch (unicode) {
						case 0x1f1ef:
						case 0x1f1fa:
						case 0x1f1eb:
						case 0x1f1e9:
						case 0x1f1ee:
						case 0x1f1ec:
						case 0x1f1ea:
						case 0x1f1f7:
						case 0x1f1e8:
						case 0x1f1f0:
							icon = 1;
							break;
						default:
							followSkip = 0;
							break;
						}
						skip += followSkip;
					}
				}
			}

			if (icon > 0) { // Emoji
				for (int j = i; j < chars.length && j < i + skip; j++) {
					sb.append(String.format("\\u%04x", (int) chars[j]));
				}
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static void setupCommonActionBar(final SherlockActivity activity,
			int titleRes) {
		setupCommonActionBar(activity, activity.getSupportActionBar(), titleRes);
	}

	public static void setupCommonActionBar(
			final SherlockFragmentActivity activity, int titleRes) {
		setupCommonActionBar(activity, activity.getSupportActionBar(), titleRes);
	}

	private static void setupCommonActionBar(final Activity activity,
			ActionBar actionbar, int titleRes) {
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);

		View viewTitleBar = activity.getLayoutInflater().inflate(
				R.layout.common_actionbar, null);

		actionbar.setCustomView(viewTitleBar, lp);
		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setDisplayShowCustomEnabled(true);

		viewTitleBar.findViewById(R.id.left_btn).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						activity.finish();
					}
				});

		viewTitleBar.findViewById(R.id.right_btn).setVisibility(View.GONE);

		((TextView) viewTitleBar.findViewById(android.R.id.title))
				.setText(titleRes);
	}

	public static void createShortCut(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			// 当前应用的版本名称
			String versionName = info.versionName;
			// 当前版本的版本号
			int versionCode = info.versionCode;
			// 当前版本的包名
			String packageNames = info.packageName;

			String sign = packageNames + "-" + versionName + "-" + versionCode;

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(context);

			String preSign = sp.getString("app_version", "");

			if (!sign.equals(preSign)) {
				Editor e = sp.edit();
				e.putString("app_version", sign);
				e.commit();

				Intent intent = new Intent(
						"com.android.launcher.action.INSTALL_SHORTCUT");
				intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
						context.getString(R.string.app_name));
				intent.putExtra("duplicate", false);
				ComponentName comp = new ComponentName(
						context.getApplicationContext(), StartupActivity.class);
				intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
						Intent.ACTION_MAIN).setComponent(comp));
				ShortcutIconResource res = Intent.ShortcutIconResource
						.fromContext(context.getApplicationContext(),
								R.drawable.ic_launcher);
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, res);
				context.sendBroadcast(intent);
			}
		} catch (NameNotFoundException e) {
		}
	}

	private static final DateFormat DF_level_timeOnly = new SimpleDateFormat(
			"HH:mm", Locale.CHINA);
	private static final DateFormat DF_level_month = new SimpleDateFormat(
			"M/d HH:mm", Locale.CHINA);
	private static final DateFormat DF_level_year = new SimpleDateFormat(
			"yyyy/M/d HH:mm", Locale.CHINA);

	private static long time_nextDay = -1;
	private static long time_today = -1;
	private static long time_thisYear = -1;

	public static String getLeveledTime(long time) {
		if (time_nextDay == -1 || System.currentTimeMillis() >= time_nextDay) {
			Calendar ca = Calendar.getInstance();
			int year = ca.get(Calendar.YEAR);
			int month = ca.get(Calendar.MONTH);
			int day = ca.get(Calendar.DAY_OF_MONTH);
			ca.set(year, month, day, 0, 0, 0);
			ca.set(Calendar.MILLISECOND, 0);
			time_today = ca.getTimeInMillis();
			time_nextDay = time_today + 24 * 60 * 60 * 1000;
			ca.set(year, 1, 1, 0, 0, 0);
			ca.set(Calendar.MILLISECOND, 0);
			time_thisYear = ca.getTimeInMillis();
		}

		if (time < time_thisYear) {
			return DF_level_year.format(new Date(time));
		} else if (time < time_today) {
			return DF_level_month.format(new Date(time));
		}
		return DF_level_timeOnly.format(new Date(time));
	}

	private static final DateFormat DF_level_dateOnly = new SimpleDateFormat(
			"MM-dd", Locale.CHINA);

	public static String getNiceTime(long time) {
		long currentTime = System.currentTimeMillis();
		long delta = currentTime - time;
		long minute = delta / 1000 / 60;
		if (minute < 1) {
			return "刚刚";
		}
		if (minute < 60) {
			return minute + "分钟前";
		}
		long hour = minute / 60;
		if (hour < 24) {
			return hour + "小时前";
		}
		long day = hour / 24;
		if (day == 1) {
			return "昨天";
		}

		return DF_level_dateOnly.format(new Date(time));
	}

	/**
	 * a copy from org.apache.commons.lang.StringUtils
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}
}
