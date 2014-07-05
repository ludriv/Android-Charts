package co.ludriv.androidcharts.views;

import co.ludriv.androidcharts.R;
import co.ludriv.androidcharts.evaluators.PointFEvaluator;
import co.ludriv.androidcharts.listeners.PieChartSegmentListener;
import co.ludriv.androidcharts.models.PieChartSegment;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
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
	
	private final int 	HIGHLIGHT_MODE_ONE		= 0;
	private final int 	HIGHLIGHT_MODE_MANY		= 1;
	
	private Context _context;
	
	private int 	_maxDegrees;
	private int		_defaultColor;
	
	private float 	_donutStrokeWidth;
	private int		_highlightAnimDuration;
	private int		_highlightMode;
	
	private Paint	_paint;
	private RectF	_rectBounds;
	
	private int 	_width;
	private int 	_height;
	
	private int		_viewPadding;
	
	private int 	_startAngle;
	private int 	_diameter;
	
	
	private PieChartSegmentListener _segmentListener;
	
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
		    
		    _highlightMode = a.getInt(R.styleable.PieChartView_highlightMode, HIGHLIGHT_MODE_ONE);
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
			
			startAngle += portionView._cAngle;
		}
		
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
	}
	
	/**
	 * Add segment to the pie chart
	 * 
	 * @param percent from 0 to 1
	 * @param color resource color id
	 * @param label title of segment
	 */
	public void addSegment(double percent, int color, String label)
	{
		if (percent < 0)
			percent = 0;
		else if (percent > 1)
			percent = 1;
		
		PieChartSegmentView segmentView = new PieChartSegmentView(_context);
		segmentView.setSegmentData(new PieChartSegment(percent, color, label));
		addView(segmentView);
	}
	
	public void setSegmentListener(PieChartSegmentListener listener)
	{
		_segmentListener = listener;
	}
	
	public void setStartAngle(int angle)
	{
		_startAngle = angle;
	}
	
	private void deselectOtherSegments(PieChartSegmentView exceptSegmentView)
	{
		int count = getChildCount();
		for (int i = 0; i < count; i++)
		{
			PieChartSegmentView segmentView = (PieChartSegmentView) getChildAt(i);
			if (!segmentView.equals(exceptSegmentView) && segmentView.isHighlighted())
			{
				segmentView.close();
			}
		}
	}
	
	public void closeAllChildren()
	{
		int count = getChildCount();
		for (int i = 0; i < count; i++)
		{
			PieChartSegmentView segmentView = (PieChartSegmentView) getChildAt(i);
			if (segmentView.isHighlighted())
			{
				segmentView.close();
			}
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
		private float 	_cAngle;
		private float 	_cStartAngle;
		
		private Canvas  _cRefCanvas;
		private Bitmap	_cRefBitmap;
		private PointF	_cOriginPoint;
		
		private PieChartSegment _cSegment;
		private boolean	_isHighlighted;
		

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
			
			_isHighlighted = false;
		}
		
		public void setSegmentData(PieChartSegment segment)
		{
			_cSegment = segment;
			_cAngle = (float) (_cSegment.getPercent() * _maxDegrees);
			_cPaint.setColor(_cSegment.getColor());
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
			
			_cRefCanvas.drawArc(_cRectF, _cStartAngle, _cAngle, true, _cPaint);
			
			if (_donutStrokeWidth > 0)
			{
				_cClearRectF.set(_cRectF.left + _donutStrokeWidth, _cRectF.top + _donutStrokeWidth, _cRectF.right - _donutStrokeWidth, _cRectF.bottom - _donutStrokeWidth);
				_cRefCanvas.drawArc(_cClearRectF, _cStartAngle - 2, _cAngle + 4, true, _cClearPaint);
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
				
				if (_highlightMode == HIGHLIGHT_MODE_ONE)
				{
					deselectOtherSegments(this);
				}
				
				setSelected(!_isHighlighted);
			}
			
			return super.onTouchEvent(event);
		}

		public void setSelected(final boolean isSelected)
		{
			PointF fromPoint = this._cOriginPoint;
			PointF toPoint = new PointF(EXPANDED_MIN_OFFSET, EXPANDED_MIN_OFFSET);
			
			if (isSelected)
			{
				// open
				float midAngle = (_cStartAngle * 2 + _cAngle) / 2;
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
				animator.addListener(new AnimatorListener()
				{
					@Override
					public void onAnimationStart(Animator animation)
					{
					}
					
					@Override
					public void onAnimationRepeat(Animator animation)
					{
					}
					
					@Override
					public void onAnimationEnd(Animator animation)
					{
						_isHighlighted = isSelected;
						
						if (_segmentListener != null)
						{
							if (isSelected)
							{
								_segmentListener.onSegmentSelected(PieChartView.this, _cSegment);
							}
							else
							{
								_segmentListener.onSegmentDeselected(PieChartView.this, _cSegment);
							}
						}
					}
					
					@Override
					public void onAnimationCancel(Animator animation)
					{
					}
				});
				animator.start();
			}
			else
			{
				setOrigin(toPoint);
				_isHighlighted = isSelected;
				
				if (_segmentListener != null)
				{
					if (isSelected)
					{
						_segmentListener.onSegmentSelected(PieChartView.this, _cSegment);
					}
					else
					{
						_segmentListener.onSegmentDeselected(PieChartView.this, _cSegment);
					}
				}
			}
		}
		
		public void open()
		{
			setSelected(true);
		}
		
		public void close()
		{
			setSelected(false);
		}
		
		public boolean isHighlighted()
		{
			return _isHighlighted;
		}
	}

	
}
