package mynuaa.whatever;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchEater extends FrameLayout {

	public TouchEater(Context context) {
		super(context);
	}

	public TouchEater(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchEater(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

}
