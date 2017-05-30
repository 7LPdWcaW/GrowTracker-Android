package me.anon.lib.helper;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.ArrayList;

import me.anon.model.Action;
import me.anon.model.Plant;
import me.anon.model.Water;

/**
 * Helper class used for generating statistics for plant
 */
public class StatsHelper
{
	private static ValueFormatter formatter = new ValueFormatter()
	{
		@Override public String getFormattedValue(float value)
		{
			return String.format("%.2f", value);
		}
	};

	/**
	 * Generates and sets the input watering data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setInputData(Plant plant, LineChart chart, String[] additionalRef)
	{
		ArrayList<Entry> inputVals = new ArrayList<>();
		ArrayList<Entry> runoffVals = new ArrayList<>();
		ArrayList<Entry> averageVals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		float totalIn = 0;
		float totalOut = 0;
		float ave = 0;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water)
			{
				if (((Water)action).getPh() != null)
				{
					inputVals.add(new Entry(((Water)action).getPh().floatValue(), index));
					min = Math.min(min, ((Water)action).getPh().floatValue());
					max = Math.max(max, ((Water)action).getPh().floatValue());

					totalIn += ((Water)action).getPh().floatValue();
				}

				if (((Water)action).getRunoff() != null)
				{
					runoffVals.add(new Entry(((Water)action).getRunoff().floatValue(), index));
					min = Math.min(min, ((Water)action).getRunoff().floatValue());
					max = Math.max(max, ((Water)action).getRunoff().floatValue());

					totalOut += ((Water)action).getRunoff().floatValue();
				}

				xVals.add("");

				float aveIn = totalIn;
				float aveOut = totalOut;
				if (index > 0)
				{
					aveIn /= (float)index;
					aveOut /= (float)index;
				}

				index++;
			}
		}

		if (chart != null)
		{
			LineDataSet dataSet = new LineDataSet(inputVals, "Input PH");
			dataSet.setDrawCubic(true);
			dataSet.setLineWidth(1.0f);
			dataSet.setDrawCircleHole(false);
			dataSet.setCircleColor(0xffffffff);
			dataSet.setValueTextColor(0xffffffff);
			dataSet.setCircleSize(2.0f);
			dataSet.setValueTextSize(8.0f);
			dataSet.setValueFormatter(formatter);

			LineDataSet runoffDataSet = new LineDataSet(runoffVals, "Runoff PH");
			runoffDataSet.setDrawCubic(true);
			runoffDataSet.setLineWidth(1.0f);
			runoffDataSet.setDrawCircleHole(false);
			runoffDataSet.setColor(0xffFFF9C4);
			runoffDataSet.setCircleColor(0xffFFF9C4);
			runoffDataSet.setValueTextColor(0xffFFF9C4);
			runoffDataSet.setCircleSize(2.0f);
			runoffDataSet.setValueTextSize(8.0f);
			runoffDataSet.setValueFormatter(formatter);

			LineDataSet averageDataSet = new LineDataSet(averageVals, "Average PH");
			averageDataSet.setDrawCubic(true);
			averageDataSet.setLineWidth(1.0f);
			averageDataSet.setDrawCircleHole(false);
			averageDataSet.setColor(0xffffffff);
			averageDataSet.setCircleSize(0.0f);
			averageDataSet.setValueTextSize(0.0f);
			averageDataSet.setValueFormatter(null);

			ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
			dataSets.add(dataSet);
			dataSets.add(runoffDataSet);

			LineData lineData = new LineData(xVals, dataSets);
			lineData.setValueFormatter(formatter);

			chart.setBackgroundColor(0xff006064);
			chart.setGridBackgroundColor(0xff006064);
			chart.setDrawGridBackground(false);
			chart.setHighlightEnabled(false);
			chart.getLegend().setTextColor(0xffffffff);
			chart.getAxisLeft().setTextColor(0xffffffff);
			chart.getAxisRight().setEnabled(false);
			chart.getAxisLeft().setValueFormatter(formatter);
			chart.getAxisLeft().setAxisMinValue(min - 0.5f);
			chart.getAxisLeft().setAxisMaxValue(max + 0.5f);
			chart.getAxisLeft().setStartAtZero(false);
			chart.setScaleYEnabled(false);
			chart.setDescription("");
			chart.setPinchZoom(false);
			chart.setDoubleTapToZoomEnabled(false);

			chart.setData(lineData);
		}

		if (additionalRef != null)
		{
			additionalRef[0] = String.valueOf(min);
			additionalRef[1] = String.valueOf(max);
			additionalRef[2] = String.format("%1$,.2f", (totalIn / (double)index));
		}
	}

	/**
	 * Generates and sets the ppm watering data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setPpmData(Plant plant, LineChart chart, String[] additionalRef)
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();

		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		long ave = 0;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getPpm() != null)
			{
				vals.add(new Entry(((Water)action).getPpm().floatValue(), index++));
				xVals.add("");

				min = Math.min(min, ((Water)action).getPpm().longValue());
				max = Math.max(max, ((Water)action).getPpm().longValue());
				ave += ((Water)action).getPpm();
			}
		}

		if (chart != null)
		{
			LineDataSet dataSet = new LineDataSet(vals, "PPM");
			dataSet.setDrawCubic(true);
			dataSet.setLineWidth(1.0f);
			dataSet.setDrawCircleHole(false);
			dataSet.setCircleColor(0xffffffff);
			dataSet.setValueTextColor(0xffffffff);
			dataSet.setCircleSize(2.0f);
			dataSet.setValueTextSize(8.0f);
			dataSet.setColor(0xffA7FFEB);

			chart.setBackgroundColor(0xff1B5E20);
			chart.setGridBackgroundColor(0xff1B5E20);
			chart.setDrawGridBackground(false);
			chart.setHighlightEnabled(false);
			chart.getLegend().setEnabled(false);
			chart.getAxisLeft().setTextColor(0xffffffff);
			chart.getAxisRight().setEnabled(false);
			chart.getAxisLeft().setXOffset(8.0f);
			chart.setScaleYEnabled(false);
			chart.setDescription("");
			chart.setPinchZoom(false);
			chart.setDoubleTapToZoomEnabled(false);
			chart.setData(new LineData(xVals, dataSet));
		}

		if (additionalRef != null)
		{
			additionalRef[0] = String.valueOf(min);
			additionalRef[1] = String.valueOf(max);
			additionalRef[2] = String.format("%1$,.2f", (ave / (double)index));
		}
	}

	/**
	 * Generates and sets the temperature data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setTempData(Plant plant, LineChart chart, String[] additionalRef)
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		float min = -100f;
		float max = 100f;
		float ave = 0;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getTemp() != null)
			{
				vals.add(new Entry(((Water)action).getTemp().floatValue(), index++));
				xVals.add("");

				min = Math.min(min, ((Water)action).getTemp().floatValue());
				max = Math.max(max, ((Water)action).getTemp().floatValue());
				ave += ((Water)action).getTemp().floatValue();
			}
		}

		if (chart != null)
		{
			LineDataSet dataSet = new LineDataSet(vals, "Temperature");
			dataSet.setDrawCubic(true);
			dataSet.setLineWidth(1.0f);
			dataSet.setDrawCircleHole(false);
			dataSet.setCircleColor(0xffffffff);
			dataSet.setValueTextColor(0xffffffff);
			dataSet.setCircleSize(2.0f);
			dataSet.setValueTextSize(8.0f);
			dataSet.setValueFormatter(formatter);

			LineData lineData = new LineData(xVals, dataSet);
			lineData.setValueFormatter(formatter);

			chart.setBackgroundColor(0xff311B92);
			chart.setGridBackgroundColor(0xff311B92);
			chart.setDrawGridBackground(false);
			chart.setHighlightEnabled(false);
			chart.getLegend().setEnabled(false);
			chart.getAxisLeft().setTextColor(0xffffffff);
			chart.getAxisRight().setEnabled(false);
			chart.getAxisLeft().setValueFormatter(formatter);
			chart.getAxisLeft().setXOffset(8.0f);
			chart.getAxisLeft().setAxisMinValue(min - 5f);
			chart.getAxisLeft().setAxisMaxValue(max + 5f);
			chart.getAxisLeft().setStartAtZero(false);
			chart.setScaleYEnabled(false);
			chart.setDescription("");
			chart.setPinchZoom(false);
			chart.setDoubleTapToZoomEnabled(false);
			chart.setData(lineData);
		}

		if (additionalRef != null)
		{
			additionalRef[0] = String.valueOf(min);
			additionalRef[1] = String.valueOf(max);
			additionalRef[2] = String.format("%1$,.2f", (ave / (double)index));
		}
	}
}
