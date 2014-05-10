package mynuaa.whatever;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SmartImageView extends ImageView {

	public SmartImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int mH = this.getSuggestedMinimumHeight();
		int mW = this.getSuggestedMinimumWidth();

		int cH = this.getMeasuredHeight();
		int cW = this.getMeasuredWidth();
		if (cH < mH || cW < mW) {
			int wms = MeasureSpec.makeMeasureSpec(mW, MeasureSpec.EXACTLY);
			int hms = MeasureSpec.makeMeasureSpec(mH, MeasureSpec.EXACTLY);
			super.onMeasure(wms, hms);
		}
	}

}
