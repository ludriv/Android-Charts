package co.ludriv.androidcharts.views;

import java.util.ArrayList;

import co.ludriv.androidcharts.R;
import co.ludriv.androidcharts.evaluators.PointFEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class PieChartView extends ViewGroup
{
	private final int 	DEFAULT_MAX_DEGREES		= 360;
	private final int 	DEFAULT_START_ANGLE 	= -90;
	private final int 	DEFAULT_COLOR			= Color.DKGRAY;
	private final int 	DEFAULT_STROKE_WIDTH	= 0;
	private final int	DEFAULT_HIGHLIGHT_ANIM_DURATION = 250;
	
	private Context _context;
	
	private int 	_maxDegrees;
	private int		_defaultColor;
	
	private float 	_donutStrokeWidth;
	private int		_highlightAnimDuration;
	
	private Paint	_paint;
	private RectF	_rectBounds;
	
	private int 	_width;
	private int 	_height;
	
	private int		_viewPadding;
	
	private int 	_startAngle;
	private int 	_diameter;
	
	
	private ArrayList<Portion> _portions = new ArrayList<PieChartView.Portion>();
	
	
	public PieChartView(Context context)
	{
		super(context);
		_context = context;
	}
	
	public PieChartView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		_context = context;
		
		_paint = new Paint();
		
		_width = 0;
		_height = 0;
		
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.PieChartView,
		        0, 0);
		
		try 
		{
			_maxDegrees = a.getInt(R.styleable.PieChartView_maxDegrees, DEFAULT_MAX_DEGREES);
		    _defaultColor = a.getColor(R.styleable.PieChartView_defaultColor, DEFAULT_COLOR);
		    
		    _startAngle = a.getInt(R.styleable.PieChartView_startAngle, DEFAULT_START_ANGLE);
		    
		    _donutStrokeWidth = a.getDimension(R.styleable.PieChartView_donutStrokeWidth, DEFAULT_STROKE_WIDTH);
		    _highlightAnimDuration = a.getInt(R.styleable.PieChartView_highlightAnimDuration, DEFAULT_HIGHLIGHT_ANIM_DURATION);
		} 
		finally 
		{
			a.recycle();
		}
		
		
		_paint.setStyle(Paint.Style.FILL);
		_paint.setAntiAlias(true);
		_paint.setColor(_defaultColor);
		
		_viewPadding = 20;
		
		_rectBounds = new RectF();
		
		setBackgroundColor(Color.TRANSPARENT);
		setWillNotDraw(false);
	}

	public PieChartView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		
		_context = context;
	}

	/**
	 * 
	 * @param percent 0..1
	 * @param color
	 * @param label
	 */
	public void addValue(double percent, int color, String label)
	{
		if (percent < 0)
			percent = 0;
		else if (percent > 1)
			percent = 1;
		
		PieChartSegmentView portionView = new PieChartSegmentView(_context);
		portionView.setPortion(Portion.newObject(percent, color, label));
		addView(portionView);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int mWidth = MeasureSpec.getSize(widthMeasureSpec); 
		int mHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		int realWidth = mWidth - getPaddingLeft() - getPaddingRight();
		int realHeight = mHeight - getPaddingTop() - getPaddingBottom();
		
		_width = realWidth;
		_height = realHeight;
		
		_diameter = Math.min(_width, _height);

		float left = 0;
		float top = 0;
		float right = _diameter;
		float bottom = _diameter;
		
		if (_width > _diameter)
			left = (_width - _diameter) / 2;
		
		if (_height > _diameter)
			top = (_height - _diameter) / 2;
		
		
		_rectBounds.set(left + _viewPadding, top + _viewPadding, right + left -_viewPadding, bottom + top -_viewPadding);
		
		setMeasuredDimension(_width, _height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		for (int i = 0; i < getChildCount(); i++)
		{
			getChildAt(i).dispatchTouchEvent(ev);
		}
		return false;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		float startAngle = _startAngle;
		
		for (int i = 0; i < getChildCount(); i++)
		{
			PieChartSegmentView portionView = (PieChartSegmentView) getChildAt(i);
			portionView.setRectF(_rectBounds);
			portionView.setStartAngle(startAngle);
			portionView.draw(canvas);
			
			startAngle += portionView._cRadius;
		}
		
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void setStartAngle(int angle)
	{
		_startAngle = angle;
	}
	
	private static class Portion
	{
		protected double percent;
		protected int color;
		protected String label;
		
		public static Portion newObject(double percent, int color, String label)
		{
			Portion p = new Portion();
			p.percent = percent;
			p.color = color;
			p.label = label;
			return p;
		}
	}
	
	private class PieChartSegmentView extends View
	{
		private final static int EXPANDED_MIN_OFFSET = 0;
		private final static int EXPANDED_MAX_OFFSET = 12;
		
		private Paint 	_cPaint;
		private Paint	_cClearPaint;
		private Paint 	_cTempPaint;
		private RectF	_cRectF;
		private RectF	_cClearRectF;
		private float 	_cRadius;
		private float 	_cStartAngle;
		
		private Portion _cPortion;
		private Canvas  _cRefCanvas;
		private Bitmap	_cRefBitmap;

		private PointF	_cOriginPoint;
		

		public PieChartSegmentView(Context context)
		{
			super(context);
			
			_cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			_cPaint.setStyle(Paint.Style.FILL);
			
			_cClearPaint = new Paint();
			_cClearPaint.setAntiAlias(true);
			_cClearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			_cClearPaint.setStyle(Paint.Style.FILL);
			
			_cClearRectF = new RectF();
			
			_cTempPaint = new Paint();
			
			_cRefCanvas = new Canvas();
			
			_cOriginPoint = new PointF(0, 0);
		}
		
		public void setPortion(Portion portion)
		{
			_cPortion = portion;
			_cRadius = (float) (_cPortion.percent * _maxDegrees);
			_cPaint.setColor(portion.color);
		}
		
		public void setRectF(RectF rectF)
		{
			_cRectF = rectF;
		}
		
		public void setStartAngle(float startAngle)
		{
			_cStartAngle = startAngle;
		}
		
		public void setOrigin(PointF pointF)
		{
			_cOriginPoint.x = pointF.x;
			_cOriginPoint.y = pointF.y;
			
			PieChartView.this.invalidate();
		}
		
		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			
			if (_cRefBitmap == null || _cRefBitmap.getWidth() != canvas.getWidth() || _cRefBitmap.getHeight() != canvas.getHeight())
			{
				_cRefBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
			}
			
			_cRefCanvas.setBitmap(_cRefBitmap);
			
			_cRefCanvas.drawArc(_cRectF, _cStartAngle, _cRadius, true, _cPaint);
			
			if (_donutStrokeWidth > 0)
			{
				_cClearRectF.set(_cRectF.left + _donutStrokeWidth, _cRectF.top + _donutStrokeWidth, _cRectF.right - _donutStrokeWidth, _cRectF.bottom - _donutStrokeWidth);
				_cRefCanvas.drawArc(_cClearRectF, _cStartAngle - 2, _cRadius + 4, true, _cClearPaint);
			}
			
			canvas.drawBitmap(_cRefBitmap, _cOriginPoint.x, _cOriginPoint.y, _cTempPaint); // 0, 0
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			if (_cRefBitmap == null)
			{
				return false;
			}
			
			int pixelColor = _cRefBitmap.getPixel((int) event.getX(), (int) event.getY());
			int pixelAlpha = Color.alpha(pixelColor);
			
			if (pixelAlpha != 0)
			{
				// touch!
//				Log.d("Android-Charts-Pie", "touch: " + _cPortion.label);
				
				PointF fromPoint = this._cOriginPoint;
				PointF toPoint = new PointF(EXPANDED_MIN_OFFSET, EXPANDED_MIN_OFFSET);

				if (fromPoint.equals(EXPANDED_MIN_OFFSET, EXPANDED_MIN_OFFSET))
				{
					// open
					float midAngle = (_cStartAngle * 2 + _cRadius) / 2;
					float x = (float) Math.cos(Math.toRadians(midAngle));
					float y = (float) Math.sin(Math.toRadians(midAngle));
					
					toPoint.set(x * EXPANDED_MAX_OFFSET, y * EXPANDED_MAX_OFFSET);
				}
				else
				{
					// close
					toPoint.set(EXPANDED_MIN_OFFSET, EXPANDED_MIN_OFFSET);
				}
				
				if (_highlightAnimDuration > 0)
				{
					ObjectAnimator animator = ObjectAnimator.ofObject(this, "origin", new PointFEvaluator(), fromPoint, toPoint);
					animator.setDuration(_highlightAnimDuration);
					animator.start();
				}
				else
				{
					setOrigin(toPoint);
				}
			}
			
			return super.onTouchEvent(event);
		}
	}

	
}
