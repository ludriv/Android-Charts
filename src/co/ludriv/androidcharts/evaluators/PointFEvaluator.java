package co.ludriv.androidcharts.evaluators;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

public class PointFEvaluator implements TypeEvaluator<PointF>
{

	@Override
	public PointF evaluate(float fraction, PointF startValue, PointF endValue)
	{
		PointF pointF = new PointF();
		
		pointF.x = startValue.x + ((endValue.x - startValue.x) * fraction);
		pointF.y = startValue.y + ((endValue.y - startValue.y) * fraction);
		
		return pointF;
	}

}
