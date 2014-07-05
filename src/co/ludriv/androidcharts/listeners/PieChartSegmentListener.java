package co.ludriv.androidcharts.listeners;

import co.ludriv.androidcharts.models.PieChartSegment;
import co.ludriv.androidcharts.views.PieChartView;

public interface PieChartSegmentListener
{
	public void onSegmentSelected(PieChartView parent, PieChartSegment segment);
	
	public void onSegmentDeselected(PieChartView parent, PieChartSegment segment);
}
