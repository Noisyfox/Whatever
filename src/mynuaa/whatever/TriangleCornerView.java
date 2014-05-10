package mynuaa.whatever;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TriangleCornerView extends View {

	Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
	Path path = new Path();

	public TriangleCornerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.Triangle);

		int c = typedArray.getColor(R.styleable.Triangle_color, Color.BLACK);
		typedArray.recycle();

		p.setStyle(Style.FILL);
		p.setColor(c);
	}

	public void setColor(int color) {
		p.setColor(color);
	}

	public int getColor() {
		return p.getColor();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();
		path.reset();
		path.moveTo(w, h);
		path.lineTo(w, 0);
		path.lineTo(0, h);
		path.lineTo(w, h);
		canvas.drawPath(path, p);
	}
}
