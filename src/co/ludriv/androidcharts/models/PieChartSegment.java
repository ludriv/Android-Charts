package co.ludriv.androidcharts.models;

public class PieChartSegment
{
	private double 	_percent;
	
	private int 	_color;
	
	private String 	_label;
	
	public PieChartSegment(double percent, int color, String label)
	{
		_percent = percent;
		_color = color;
		_label = label;
	}

	/**
	 * @return the percent
	 */
	public double getPercent()
	{
		return _percent;
	}

	/**
	 * @return the color
	 */
	public int getColor()
	{
		return _color;
	}

	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return _label;
	}
	
}
