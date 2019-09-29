package me.anon.lib.helper;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.ColorUtils;
import androidx.core.util.Pair;
import me.anon.grow.R;
import me.anon.lib.ext.IntUtilsKt;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.Plant;
import me.anon.model.PlantStage;
import me.anon.model.Water;

/**
 * Helper class used for generating statistics for plant
 */
public class StatsHelper
{
	public static ValueFormatter formatter = new ValueFormatter()
	{
		@Override public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler)
		{
			if (value == (int)value)
			{
				return String.format("%s", value);
			}

			return String.format("%.2f", value);
		}
	};

	public static void styleGraph(BarLineChartBase chart)
	{
		Context context = new ContextThemeWrapper(chart.getContext(), R.style.AppTheme);
		chart.setDrawGridBackground(false);
		chart.setGridBackgroundColor(0x00ffffff);
		chart.getAxisLeft().setDrawGridLines(false);
		chart.getXAxis().setDrawGridLines(false);
		chart.getLegend().setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
		chart.getLegend().setTextSize(12f);
		chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		chart.getXAxis().setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
		chart.getXAxis().setTextSize(12f);
		chart.getAxisRight().setEnabled(false);
		chart.getAxisLeft().setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
		chart.getAxisLeft().setTextSize(12f);
		chart.getAxisLeft().setValueFormatter(new YAxisValueFormatter()
		{
			@Override public String getFormattedValue(float value, YAxis yAxis)
			{
				if (value == (int)value)
				{
					return String.format("%s", value);
				}

				return String.format("%.2f", value);
			}
		});
		chart.getAxisLeft().setStartAtZero(false);
		chart.getAxisRight().setValueFormatter(new YAxisValueFormatter()
		{
			@Override public String getFormattedValue(float value, YAxis yAxis)
			{
				if (value == (int)value)
				{
					return String.format("%s", value);
				}

				return String.format("%.2f", value);
			}
		});
		chart.setScaleYEnabled(false);
		chart.setDescription("");
		chart.getAxisLeft().setXOffset(8.0f);
		chart.getLegend().setWordWrapEnabled(true);
		chart.setTouchEnabled(true);
		chart.setHighlightPerTapEnabled(true);
		chart.setMarkerView(new MarkerView(context, R.layout.chart_marker)
		{
			@Override
			public void refreshContent(Entry e, Highlight highlight)
			{
				((TextView)findViewById(R.id.content)).setText("" + e.getVal());
				((TextView)findViewById(R.id.content)).setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, findViewById(R.id.content).getContext()));
			}

			@Override public int getXOffset(float xpos)
			{
				return -(getWidth() / 2);
			}

			@Override public int getYOffset(float ypos)
			{
				return -getHeight();
			}
		});
	}

	public static void styleDataset(Context context, LineDataSet data, int colour)
	{
		context = new ContextThemeWrapper(context, R.style.AppTheme);
		data.setValueTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
		data.setCircleColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
		data.setDrawCubic(true);
		data.setLineWidth(2.0f);
		data.setDrawCircleHole(true);
		data.setColor(colour);
		data.setCircleColor(colour);
		data.setCircleSize(4.0f);
		data.setDrawHighlightIndicators(true);
		data.setHighlightEnabled(true);
		data.setHighlightLineWidth(2f);
		data.setHighLightColor(ColorUtils.setAlphaComponent(colour, 96));
		data.setDrawValues(false);
		data.setValueFormatter(formatter);
	}

	public static void setAdditiveData(Plant plant, @NonNull Context context, @Nullable LineChart chart, @NonNull Set<String> checkedAdditives)
	{
		ArrayList<Action> actions = plant.getActions();
		ArrayList<Pair<String, ArrayList<Entry>>> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		final Set<String> additiveNames = new HashSet<>();
		LineData data = new LineData();
		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (Action action : actions)
		{
			if (action instanceof Water)
			{
				List<Additive> actionAdditives = ((Water)action).getAdditives();
				for (Additive additive : actionAdditives)
				{
					additiveNames.add(additive.getDescription());
					min = Math.min(min, additive.getAmount());
					max = Math.max(max, additive.getAmount());
				}

				PlantStage stage = null;
				long changeDate = 0;
				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
				while (iterator.hasPrevious())
				{
					PlantStage key = iterator.previous();
					Action changeAction = stageTimes.get(key);
					if (action.getDate() > changeAction.getDate())
					{
						stage = key;
						changeDate = changeAction.getDate();
					}
				}

				long difference = action.getDate() - changeDate;
				if (stage != null)
				{
					xVals.add(((int)TimeHelper.toDays(difference) + "" + context.getString(stage.getPrintString()).charAt(0)).toLowerCase());
				}
				else
				{
					xVals.add("");
				}
			}
		}

		ArrayList<LineDataSet> dataSets = new ArrayList<>();
		for (String additiveName : checkedAdditives)
		{
			int index = 0;
			ArrayList<Entry> additiveValues = new ArrayList<>();
			for (Action action : actions)
			{
				if (action instanceof Water)
				{
					boolean found = false;
					for (Additive additive : ((Water)action).getAdditives())
					{
						if (additiveName.equals(additive.getDescription()))
						{
							found = true;
							additiveValues.add(new Entry(additive.getAmount().floatValue(), index));
						}
					}

					index++;
				}
			}

			LineDataSet dataSet = new LineDataSet(additiveValues, additiveName);

			String[] colours = chart.getResources().getStringArray(R.array.stats_colours);
			ArrayList<String> namesList = new ArrayList<>(additiveNames);
			StatsHelper.styleDataset(context, dataSet, dataSets.size() < colours.length ? Color.parseColor(colours[namesList.indexOf(additiveName)]) : (additiveName.hashCode() + new Random().nextInt(0xffffff)));

			dataSet.setValueFormatter(StatsHelper.formatter);
			dataSets.add(dataSet);
		}

		LineData lineData = new LineData(xVals, dataSets);
		lineData.setValueFormatter(StatsHelper.formatter);
		lineData.setValueTextColor(IntUtilsKt.resolveColor(android.R.attr.textColorSecondary, context));

		styleGraph(chart);

		chart.setData(lineData);
	}

	/**
	 * Generates and sets the input watering data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setInputData(Plant plant, @NonNull Context context, @Nullable LineChart chart, String[] additionalRef)
	{
		ArrayList<Entry> inputVals = new ArrayList<>();
		ArrayList<Entry> runoffVals = new ArrayList<>();
		ArrayList<Entry> averageVals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();
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

				PlantStage stage = null;
				long changeDate = 0;
				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
				while (iterator.hasPrevious())
				{
					PlantStage key = iterator.previous();
					Action changeAction = stageTimes.get(key);
					if (action.getDate() > changeAction.getDate())
					{
						stage = key;
						changeDate = changeAction.getDate();
					}
				}

				long difference = action.getDate() - changeDate;
				if (stage != null)
				{
					xVals.add(((int)TimeHelper.toDays(difference) + "" + context.getString(stage.getPrintString()).charAt(0)).toLowerCase());
				}
				else
				{
					xVals.add("");
				}

				index++;
			}
		}

		if (chart != null)
		{
			LineDataSet dataSet = new LineDataSet(inputVals, context.getString(R.string.stat_input_ph));
			styleDataset(context, dataSet, ColorTemplate.COLORFUL_COLORS[0]);

			LineDataSet runoffDataSet = new LineDataSet(runoffVals, context.getString(R.string.stat_runoff_ph));
			styleDataset(context, dataSet, ColorTemplate.COLORFUL_COLORS[1]);

			LineDataSet averageDataSet = new LineDataSet(averageVals, context.getString(R.string.stat_average_ph));
			styleDataset(context, dataSet, ColorTemplate.COLORFUL_COLORS[2]);
			averageDataSet.setValueFormatter(null);

			ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
			dataSets.add(dataSet);
			dataSets.add(runoffDataSet);

			LineData lineData = new LineData(xVals, dataSets);
			lineData.setValueFormatter(formatter);

			styleGraph(chart);
			chart.getAxisLeft().setAxisMinValue(min - 0.5f);
			chart.getAxisLeft().setAxisMaxValue(max + 0.5f);

			chart.setData(lineData);
		}

		if (additionalRef != null)
		{
			additionalRef[0] = String.valueOf(min);
			additionalRef[1] = String.valueOf(max);
			additionalRef[2] = String.format("%1$,.2f", (totalIn / (double)inputVals.size()));
		}
	}

	/**
	 * Generates and sets the ppm watering data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 * @param usingEc If using EC measurements = (ppm * 1000d) / 2d
	 */
	public static void setPpmData(Plant plant, @NonNull Context context, @Nullable LineChart chart, String[] additionalRef, boolean usingEc)
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();

		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		long total = 0;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getPpm() != null)
			{
				float value = ((Water)action).getPpm().floatValue();

				if (usingEc)
				{
					// PPM -> EC
					value = (value * 2.0f) / 1000.0f;
				}

				vals.add(new Entry(value, index++));
				PlantStage stage = null;
				long changeDate = 0;
				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
				while (iterator.hasPrevious())
				{
					PlantStage key = iterator.previous();
					Action changeAction = stageTimes.get(key);
					if (action.getDate() > changeAction.getDate())
					{
						stage = key;
						changeDate = changeAction.getDate();
					}
				}

				long difference = action.getDate() - changeDate;
				if (stage != null)
				{
					xVals.add(((int)TimeHelper.toDays(difference) + "" + context.getString(stage.getPrintString()).charAt(0)).toLowerCase());
				}
				else
				{
					xVals.add("");
				}

				min = Math.min(min, (long)value);
				max = Math.max(max, (long)value);
				total += value;
			}
		}

		if (chart != null)
		{
			LineDataSet dataSet = new LineDataSet(vals, usingEc ? "EC" : "PPM");
			styleDataset(context, dataSet, Color.parseColor(context.getResources().getStringArray(R.array.stats_colours)[0]));
			styleGraph(chart);
			chart.setMarkerView(new MarkerView(context, R.layout.chart_marker)
			{
				@Override
				public void refreshContent(Entry e, Highlight highlight)
				{
					String val = String.format("%.2f", e.getVal());
					if (e.getVal() == (int)e.getVal())
					{
						val = String.format("%s", (int)e.getVal());
					}

					((TextView)findViewById(R.id.content)).setText(val);
					((TextView)findViewById(R.id.content)).setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, findViewById(R.id.content).getContext()));
				}

				@Override public int getXOffset(float xpos)
				{
					return -(getWidth() / 2);
				}

				@Override public int getYOffset(float ypos)
				{
					return -getHeight();
				}
			});
			chart.getAxisLeft().setValueFormatter(new YAxisValueFormatter()
			{
				@Override public String getFormattedValue(float value, YAxis yAxis)
				{
					if (value == (int)value)
					{
						return String.format("%s", (int)value);
					}

					return String.format("%.2f", value);
				}
			});
			dataSet.setValueFormatter(new ValueFormatter()
			{
				@Override public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler)
				{
					if (value == (int)value)
					{
						return String.format("%s", (int)value);
					}

					return String.format("%.2f", value);
				}
			});

			chart.setData(new LineData(xVals, dataSet));
		}

		if (additionalRef != null)
		{
			additionalRef[0] = String.valueOf(min);
			additionalRef[1] = String.valueOf(max);
			additionalRef[2] = String.format("%1$,.2f", (total / (double)vals.size()));
		}
	}

	/**
	 * Generates and sets the temperature data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setTempData(Plant plant, @NonNull Context context, @Nullable LineChart chart, String[] additionalRef)
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();
		float min = 100f;
		float max = -100f;
		float total = 0;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getTemp() != null)
			{
				vals.add(new Entry(((Water)action).getTemp().floatValue(), index++));
				PlantStage stage = null;
				long changeDate = 0;
				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
				while (iterator.hasPrevious())
				{
					PlantStage key = iterator.previous();
					Action changeAction = stageTimes.get(key);
					if (action.getDate() > changeAction.getDate())
					{
						stage = key;
						changeDate = changeAction.getDate();
					}
				}

				long difference = action.getDate() - changeDate;
				if (stage != null)
				{
					xVals.add(((int)TimeHelper.toDays(difference) + "" + context.getString(stage.getPrintString()).charAt(0)).toLowerCase());
				}
				else
				{
					xVals.add("");
				}

				min = Math.min(min, ((Water)action).getTemp().floatValue());
				max = Math.max(max, ((Water)action).getTemp().floatValue());
				total += ((Water)action).getTemp().floatValue();
			}
		}

		if (chart != null)
		{
			LineDataSet dataSet = new LineDataSet(vals, context.getString(R.string.stat_temerature));
			styleDataset(context, dataSet, Color.parseColor(context.getResources().getStringArray(R.array.stats_colours)[0]));

			LineData lineData = new LineData(xVals, dataSet);
			lineData.setValueFormatter(formatter);

			chart.getAxisLeft().setAxisMinValue(min - 5f);
			chart.getAxisLeft().setAxisMaxValue(max + 5f);
			styleGraph(chart);
			chart.setData(lineData);
		}

		if (additionalRef != null)
		{
			additionalRef[0] = String.valueOf(min);
			additionalRef[1] = String.valueOf(max);
			additionalRef[2] = String.format("%1$,.2f", (total / (double)vals.size()));
		}
	}
}
