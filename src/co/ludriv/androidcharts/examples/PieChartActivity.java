package co.ludriv.androidcharts.examples;

import co.ludriv.androidcharts.R;
import co.ludriv.androidcharts.listeners.PieChartSegmentListener;
import co.ludriv.androidcharts.models.PieChartSegment;
import co.ludriv.androidcharts.views.PieChartView;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class PieChartActivity extends Activity implements PieChartSegmentListener
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_piechart);
		
		PieChartView pieChartView = (PieChartView) findViewById(R.id.example_piechartview);
		
		pieChartView.addSegment(0.52, Color.GREEN, "China");
		pieChartView.addSegment(0.16, Color.BLUE, "France");
		pieChartView.addSegment(0.20, Color.RED, "Brazil");
		pieChartView.addSegment(0.12, Color.MAGENTA, "Colombia");
		
		pieChartView.setSegmentListener(this);
	}

	@Override
	public void onSegmentSelected(PieChartView parent, PieChartSegment segment)
	{
		Log.d("Android-Charts", "selected segment: " + segment.getLabel());
	}

	@Override
	public void onSegmentDeselected(PieChartView parent, PieChartSegment segment)
	{
		Log.d("Android-Charts", "deselected segment: " + segment.getLabel());
	}
	
}
