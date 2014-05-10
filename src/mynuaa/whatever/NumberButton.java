package mynuaa.whatever;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.Button;

public class NumberButton extends Button {
	private float offsetX = 8, offsetY = 8;
	private int mNumber = 0;
	private boolean mNumberVisible = true;

	public NumberButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.NumberButton);
		offsetX = typedArray.getDimension(
				R.styleable.NumberButton_number_offset_x, 8);
		offsetY = typedArray.getDimension(
				R.styleable.NumberButton_number_offset_y, 8);
		typedArray.recycle();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mNumberVisible) {
			TextPaint p = getPaint();
			Align al = p.getTextAlign();
			float sz = p.getTextSize();
			p.setTextAlign(Align.RIGHT);
			p.setTextSize(getPaint().getTextSize() * 0.7f);
			canvas.drawText(String.valueOf(mNumber), getWidth() - offsetX,
					getHeight() - offsetY, p);
			p.setTextAlign(al);
			p.setTextSize(sz);
		}
	}

	public void setNumber(int number) {
		mNumber = number;
		invalidate();
	}

	public void setNumberVisible(boolean visible) {
		mNumberVisible = visible;
	}
}
