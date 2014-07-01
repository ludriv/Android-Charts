package co.ludriv.androidcharts.views;

import java.util.ArrayList;

import co.ludriv.androidcharts.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class PieChartView extends ViewGroup
{
	private final int DEFAULT_MAX			= 360;
	private final int DEFAULT_START_ANGLE 	= -90;
	private final int DEFAULT_COLOR			= Color.DKGRAY;
	private final int DEFAULT_STROKE_WIDTH	= 0;
	
	private Context _context;
	
	private int 	_max;
	private int		_defaultColor;
	
	private int 	_strokeWidth;
	
	private Paint	_paint;
	private RectF	_rectBounds;
	private Paint 	clearPaint;
	
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
			_max = a.getInt(R.styleable.PieChartView_max, DEFAULT_MAX);
		    _defaultColor = a.getColor(R.styleable.PieChartView_defaultColor, DEFAULT_COLOR);
		} 
		finally 
		{
			a.recycle();
		}
		
		
		_paint.setStyle(Paint.Style.FILL);
		_paint.setAntiAlias(true);
		_paint.setColor(_defaultColor);
		
		clearPaint = new Paint();
		clearPaint.setAntiAlias(true);
		clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		clearPaint.setStyle(Paint.Style.FILL);
	
		_startAngle = DEFAULT_START_ANGLE;
		_strokeWidth = DEFAULT_STROKE_WIDTH;
		
		_viewPadding = 20;
		
		_rectBounds = new RectF();
		
		setBackgroundColor(Color.TRANSPARENT);
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
		
		PortionPieChartView portionView = new PortionPieChartView(_context);
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
			PortionPieChartView portionView = (PortionPieChartView) getChildAt(i);
			portionView.setRectF(_rectBounds);
			portionView.setStartAngle(startAngle);
			portionView.draw(canvas);
			
			startAngle += portionView._radius;
		}
		
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		// TODO Auto-generated method stub
		
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
	
	private class PortionPieChartView extends View
	{
		private Paint 	_paint;
		private Paint	_clearPaint;
		private Paint 	_tempPaint;
		private RectF	_rectF;
		private RectF	_clearRectF;
		private float 	_radius;
		private float 	_startAngle;
		
		private Portion _portion;
		private Canvas  _refCanvas;
		private Bitmap	_refBitmap;
		

		public PortionPieChartView(Context context)
		{
			super(context);
			
			_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			_paint.setStyle(Paint.Style.FILL);
			
			_clearPaint = new Paint();
			_clearPaint.setAntiAlias(true);
			_clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			_clearPaint.setStyle(Paint.Style.FILL);
			
			_clearRectF = new RectF();
			
			_tempPaint = new Paint();
			
			_refCanvas = new Canvas();
		}
		
		public void setPortion(Portion portion)
		{
			_portion = portion;
			_radius = (float) (_portion.percent * _max);
			_paint.setColor(portion.color);
		}
		
		public void setRectF(RectF rectF)
		{
			_rectF = rectF;
		}
		
		public void setStartAngle(float startAngle)
		{
			_startAngle = startAngle;
		}
		
		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);

			if (_refBitmap == null || _refBitmap.getWidth() != canvas.getWidth() || _refBitmap.getHeight() != canvas.getHeight())
			{
				_refBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
			}
			
			_refCanvas.setBitmap(_refBitmap);
			
			_refCanvas.drawArc(_rectF, _startAngle, _radius, true, _paint);
			
			if (_strokeWidth > 0)
			{
				_clearRectF.set(_rectF.left + _strokeWidth, _rectF.top + _strokeWidth, _rectF.right - _strokeWidth, _rectF.bottom - _strokeWidth);
				_refCanvas.drawArc(_clearRectF, _startAngle - 2, _radius + 4, true, _clearPaint);
			}
			
			canvas.drawBitmap(_refBitmap, 0, 0, _tempPaint);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			int pixelColor = _refBitmap.getPixel((int) event.getX(), (int) event.getY());
			int pixelAlpha = Color.alpha(pixelColor);
			
			if (pixelAlpha != 0)
			{
				// touch!
				Log.d("Android-Charts-Pie", "touch: " + _portion.label);
			}
			
			return super.onTouchEvent(event);
		}
		
	}

	
}
