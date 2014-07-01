package co.ludriv.androidcharts.examples;

import co.ludriv.androidcharts.R;
import co.ludriv.androidcharts.views.PieChartView;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

public class PieChartActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_piechart);
		
		PieChartView pieChartView = (PieChartView) findViewById(R.id.example_piechartview);
		
		pieChartView.addValue(0.52, Color.GREEN, "China");
		pieChartView.addValue(0.16, Color.BLUE, "France");
		pieChartView.addValue(0.20, Color.YELLOW, "Colombia");
	}
}
