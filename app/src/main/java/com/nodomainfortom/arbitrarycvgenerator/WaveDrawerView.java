package com.nodomainfortom.arbitrarycvgenerator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

public class WaveDrawerView extends View {
	//Log Tags
	private static final String CLASS_TAG = "DrawFragment";
	private static final String TOUCH_EVENT_DOWN = "TouchEventDown";
	private static final String TOUCH_EVENT_UP = "TouchEventUp";
	private static final String TOUCH_EVENT_ERROR = "TouchEventError";


	//Member Variables
	Waveform mActiveWave;
	Paint mPointPaint, mLinePaint;
	float mDisplayDensityScale;
	int mLineWidthInDp;
	int mLineWidthInPixel;


	//Constructors
	public WaveDrawerView(Context context) {
		this(context, null);
	}

	public WaveDrawerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mDisplayDensityScale = getResources().getDisplayMetrics().density;
		mLineWidthInDp = 3;
		mLineWidthInPixel = (int) (mLineWidthInDp * mDisplayDensityScale);

		mLinePaint = new Paint();
		mLinePaint.setColor(0xFF00FF00);
		mLinePaint.setStrokeWidth(mLineWidthInPixel);
	}


	//Drawing Methods
	private PointF comparePointFAlongXAxis(PointF prev, PointF crnt) {
		//since PointF x-values represent time and y-values amplitude, this method is used in the
		//construction of List<PointF>'s to ensure the time values are never decreasing
		float outX, outY;

		if (crnt.x > prev.x) {
			return crnt;
		} else {
			return new PointF(prev.x, crnt.y);
		}
	}

	public void doDrawLogic(MotionEvent event) {
		PointF current, previous;
		current = new PointF(event.getX()/getWidth(), event.getY()/getHeight());
		try {
			previous = mActiveWave.getOriginalWaveData().get(mActiveWave.getOriginalWaveData().size()-1);
		} catch (IndexOutOfBoundsException ioob) {
			previous = new PointF(0,0);
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.d(CLASS_TAG, TOUCH_EVENT_DOWN + ": start of touch event");
				mActiveWave.getOriginalWaveData().clear();
				mActiveWave.getOriginalWaveData().add(current);
				invalidate();
				break;

			case MotionEvent.ACTION_MOVE:
				current = comparePointFAlongXAxis(previous, current);

				if (isPointTooClose(previous, current)) break;

				interpolateTwoPointFs(previous, current);
				mActiveWave.getOriginalWaveData().add(current);
				invalidate();
				break;

			case MotionEvent.ACTION_UP:
				current = comparePointFAlongXAxis(previous, current);

				if (isPointTooClose(previous, current)) {
					Log.d(CLASS_TAG, TOUCH_EVENT_UP + ": end of touch event");
					mActiveWave.updateInterpolatedWaveData(getContext());
					break;
				}

				interpolateTwoPointFs(previous, current);
				mActiveWave.getOriginalWaveData().add(current);
				mActiveWave.updateInterpolatedWaveData(getContext());
				invalidate();
				Log.d(CLASS_TAG, TOUCH_EVENT_UP + ": end of touch event");
				break;

			case MotionEvent.ACTION_CANCEL:
				Log.d(CLASS_TAG, TOUCH_EVENT_ERROR);
				mActiveWave.getOriginalWaveData().clear();
				invalidate();
				break;
		}
	}

	private void interpolateTwoPointFs(PointF pt1, PointF pt2) {
		if (isPointTooFar(pt1, pt2)) {
			PointF refPoint;

			refPoint = new PointF((pt1.x + pt2.x) / 2, (pt1.y + pt2.y) / 2);
			mActiveWave.getOriginalWaveData().add(refPoint);

			interpolateTwoPointFs(pt1, refPoint);

			interpolateTwoPointFs(refPoint, pt2);
		}
	}

	private boolean isPointTooClose(PointF reference, PointF point) {
		if ((point.x > reference.x - .01) && (point.x < reference.x + .01)
				&& (point.y > reference.y - .01) && (point.y < reference.y + .01)) {
			return true;
		} else return false;
	}

	private boolean isPointTooFar(PointF reference, PointF point) {
		if ((point.x < reference.x - .02) || (point.x > reference.x + .02)
				|| (point.y < reference.y - .02) || (point.y > reference.y + .02)) {
			return true;
		} else return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if ((mActiveWave == null) || (mActiveWave.getOriginalWaveData().size() == 0)) {
			return;
		}

		for (int i = 1; i < mActiveWave.getOriginalWaveData().size(); i++) {
			canvas.drawLine(mActiveWave.getOriginalWaveData().get(i-1).x * getWidth(),
					mActiveWave.getOriginalWaveData().get(i-1).y * getHeight(),
					mActiveWave.getOriginalWaveData().get(i).x * getWidth(),
					mActiveWave.getOriginalWaveData().get(i).y * getHeight(), mLinePaint);
		}
	}


	//Getters and Setters
	public Waveform getActiveWave() {
		return mActiveWave;
	}

	public void setActiveWave(Waveform wave) {
		mActiveWave = wave;
	}

	public Paint getPointPaint() {
		return mPointPaint;
	}

	public void setPointPaint(Paint pointPaint) {
		mPointPaint = pointPaint;
	}

	public float getDisplayDensityScale() {
		return mDisplayDensityScale;
	}

	public void setDisplayDensityScale(float displayDensityScale) {
		mDisplayDensityScale = displayDensityScale;
	}

	public Paint getLinePaint() {
		return mLinePaint;
	}

	public void setLinePaint(Paint linePaint) {
		mLinePaint = linePaint;
	}

	public int getLineWidthInDp() {
		return mLineWidthInDp;
	}

	public void setLineWidthInDp(int lineWidthInDp) {
		mLineWidthInDp = lineWidthInDp;
	}

	public int getLineWidthInPixel() {
		return mLineWidthInPixel;
	}

	public void setLineWidthInPixel(int lineWidthInPixel) {
		mLineWidthInPixel = lineWidthInPixel;
	}

}
