package me.anon.lib.helper;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextThemeWrapper;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import me.anon.grow.R;
import me.anon.lib.TdsUnit;
import me.anon.lib.TempUnit;
import me.anon.lib.ext.EntryExtKt;
import me.anon.lib.ext.IntUtilsKt;
import me.anon.lib.ext.NumberUtilsKt;
import me.anon.model.Action;
import me.anon.model.Garden;
import me.anon.model.HumidityChange;
import me.anon.model.Plant;
import me.anon.model.TemperatureChange;

/**
 * Helper class used for generating statistics for plant
 */
public class StatsHelper
{
	public static ValueFormatter formatter = new ValueFormatter()
	{
		@Override public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler)
		{
			return NumberUtilsKt.formatWhole(value);
		}
	};

	public static void styleGraph(BarLineChartBase chart)
	{
//		Context context = new ContextThemeWrapper(chart.getContext(), R.style.AppTheme);
//		chart.setDrawGridBackground(false);
//		chart.setGridBackgroundColor(0x00ffffff);
//		chart.getAxisLeft().setDrawGridLines(false);
//		chart.getXAxis().setDrawGridLines(false);
//		chart.getLegend().setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
//		chart.getLegend().setTextSize(12f);
//		chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//		chart.getXAxis().setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
//		chart.getXAxis().setTextSize(12f);
//		chart.getAxisRight().setEnabled(false);
//		chart.getAxisLeft().setTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
//		chart.getAxisLeft().setTextSize(12f);
//		chart.getAxisLeft().setValueFormatter(new YAxisValueFormatter()
//		{
//			@Override public String getFormattedValue(float value, YAxis yAxis)
//			{
//				return NumberUtilsKt.formatWhole(value);
//			}
//		});
//		chart.getAxisLeft().setStartAtZero(false);
//		chart.getAxisRight().setValueFormatter(new YAxisValueFormatter()
//		{
//			@Override public String getFormattedValue(float value, YAxis yAxis)
//			{
//				return NumberUtilsKt.formatWhole(value);
//			}
//		});
//		chart.setScaleYEnabled(false);
//		chart.setDescription("");
//		chart.getAxisLeft().setXOffset(8.0f);
//		chart.getLegend().setWordWrapEnabled(true);
//		chart.setTouchEnabled(true);
//		chart.setHighlightPerTapEnabled(true);
//		chart.setNoDataText(context.getString(R.string.no_data));
//		chart.setMarkerView(new MarkerView(context, R.layout.chart_marker)
//		{
//			@Override
//			public void refreshContent(Entry e, Highlight highlight)
//			{
//				((TextView)findViewById(R.id.content)).setText("" + e.getVal());
//			}
//
//			@Override public int getXOffset(float xpos)
//			{
//				return -(getWidth() / 2);
//			}
//
//			@Override public int getYOffset(float ypos)
//			{
//				return -getHeight();
//			}
//		});
	}

	public static void styleDataset(Context context, LineDataSet data, int colour)
	{
		context = new ContextThemeWrapper(context, R.style.AppTheme);
		data.setValueTextColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
		data.setCircleColor(IntUtilsKt.resolveColor(R.attr.colorAccent, context));
		data.setCubicIntensity(0.05f);
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
//		data.setValueFormatter(formatter);
	}

	public static void setAdditiveData(Plant plant, @NonNull Context context, @Nullable LineChart chart, @NonNull Set<String> checkedAdditives)
	{
//		final Unit measurement = Unit.getSelectedMeasurementUnit(context);
//		final Unit delivery = Unit.getSelectedDeliveryUnit(context);
//
//		ArrayList<Action> actions = plant.getActions();
//		ArrayList<Pair<String, ArrayList<Entry>>> vals = new ArrayList<>();
//		ArrayList<String> xVals = new ArrayList<>();
//		final Set<String> additiveNames = new HashSet<>();
//		LineData data = new LineData();
//		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();
//		double min = Double.MAX_VALUE;
//		double max = Double.MIN_VALUE;
//
//		for (Action action : actions)
//		{
//			if (action instanceof Water)
//			{
//				List<Additive> actionAdditives = ((Water)action).getAdditives();
//				for (Additive additive : actionAdditives)
//				{
//					double amount = Unit.ML.to(measurement, additive.getAmount());
//					additiveNames.add(additive.getDescription());
//					min = Math.min(min, amount);
//					max = Math.max(max, amount);
//				}
//
//				PlantStage stage = null;
//				long changeDate = 0;
//				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
//				while (iterator.hasPrevious())
//				{
//					PlantStage key = iterator.previous();
//					Action changeAction = stageTimes.get(key);
//					if (action.getDate() > changeAction.getDate())
//					{
//						stage = key;
//						changeDate = changeAction.getDate();
//					}
//				}
//
//				long difference = action.getDate() - changeDate;
//				if (stage != null)
//				{
//					xVals.add(((int)TimeHelper.toDays(difference) + "" + context.getString(stage.getPrintString()).charAt(0)).toLowerCase());
//				}
//				else
//				{
//					xVals.add("");
//				}
//			}
//		}
//
//		ArrayList<String> namesList = new ArrayList<>(additiveNames);
//		ArrayList<LineDataSet> dataSets = new ArrayList<>();
//		final String[] colours = chart.getResources().getStringArray(R.array.stats_colours);
//		for (String additiveName : checkedAdditives)
//		{
//			int index = 0;
//			int color = dataSets.size() < colours.length ? Color.parseColor(colours[namesList.indexOf(additiveName)]) : (additiveName.hashCode() + new Random().nextInt(0xffffff));
//			ArrayList<Entry> additiveValues = new ArrayList<>();
//			for (Action action : actions)
//			{
//				if (action instanceof Water)
//				{
//					for (Additive additive : ((Water)action).getAdditives())
//					{
//						if (additiveName.equals(additive.getDescription()))
//						{
//							double amount = Unit.ML.to(measurement, additive.getAmount());
//
//							Entry entry = new Entry((float)amount, index);
//							entry.setData(color);
//							additiveValues.add(entry);
//						}
//					}
//
//					index++;
//				}
//			}
//
//			LineDataSet dataSet = new LineDataSet(additiveValues, additiveName);
//
//			StatsHelper.styleDataset(context, dataSet, color);
//
//			dataSet.setValueFormatter(StatsHelper.formatter);
//			dataSets.add(dataSet);
//		}
//
//		LineData lineData = new LineData(xVals, dataSets);
//		lineData.setValueFormatter(StatsHelper.formatter);
//		lineData.setValueTextColor(IntUtilsKt.resolveColor(android.R.attr.textColorSecondary, context));
//
//		styleGraph(chart);
//
//		chart.setData(lineData);
	}

	/**
	 * Generates and sets the input watering data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setInputData(Plant plant, @Nullable Context context, @Nullable LineChart chart, String[] additionalRef)
	{
//		ArrayList<Entry> inputVals = new ArrayList<>();
//		ArrayList<Entry> runoffVals = new ArrayList<>();
//		ArrayList<Entry> averageVals = new ArrayList<>();
//		ArrayList<String> xVals = new ArrayList<>();
//		LineData data = new LineData();
//		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();
//		float min = Float.MAX_VALUE;
//		float max = Float.MIN_VALUE;
//		float totalIn = 0;
//
//		int index = 0;
//		for (Action action : plant.getActions())
//		{
//			if (action instanceof Water)
//			{
//				if (((Water)action).getPh() != null)
//				{
//					inputVals.add(new Entry(((Water)action).getPh().floatValue(), index));
//					min = Math.min(min, ((Water)action).getPh().floatValue());
//					max = Math.max(max, ((Water)action).getPh().floatValue());
//
//					totalIn += ((Water)action).getPh().floatValue();
//				}
//
//				if (((Water)action).getRunoff() != null)
//				{
//					runoffVals.add(new Entry(((Water)action).getRunoff().floatValue(), index));
//					min = Math.min(min, ((Water)action).getRunoff().floatValue());
//					max = Math.max(max, ((Water)action).getRunoff().floatValue());
//				}
//
//				PlantStage stage = null;
//				long changeDate = 0;
//				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
//				while (iterator.hasPrevious())
//				{
//					PlantStage key = iterator.previous();
//					Action changeAction = stageTimes.get(key);
//					if (action.getDate() > changeAction.getDate())
//					{
//						stage = key;
//						changeDate = changeAction.getDate();
//					}
//				}
//
//				long difference = action.getDate() - changeDate;
//				if (stage != null)
//				{
//					xVals.add(((int)TimeHelper.toDays(difference) + "" + (context != null ? context.getString(stage.getPrintString()) : stage.getEnString()).charAt(0)).toLowerCase());
//				}
//				else
//				{
//					xVals.add("");
//				}
//
//				index++;
//			}
//		}
//
//		if (chart != null && context != null)
//		{
//			LineDataSet dataSet = new LineDataSet(inputVals, context.getString(R.string.stat_input_ph));
//			styleDataset(context, dataSet, ColorTemplate.COLORFUL_COLORS[0]);
//
//			LineDataSet runoffDataSet = new LineDataSet(runoffVals, context.getString(R.string.stat_runoff_ph));
//			styleDataset(context, dataSet, ColorTemplate.COLORFUL_COLORS[1]);
//
//			LineDataSet averageDataSet = new LineDataSet(averageVals, context.getString(R.string.stat_average_ph));
//			styleDataset(context, dataSet, ColorTemplate.COLORFUL_COLORS[2]);
//			averageDataSet.setValueFormatter(null);
//
//			ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
//			dataSets.add(dataSet);
//			dataSets.add(runoffDataSet);
//
//			LineData lineData = new LineData(xVals, dataSets);
//			lineData.setValueFormatter(formatter);
//
//			styleGraph(chart);
//			chart.getAxisLeft().setAxisMinValue(min - 0.5f);
//			chart.getAxisLeft().setAxisMaxValue(max + 0.5f);
//
//			chart.setData(lineData);
//		}
//
//		if (additionalRef != null)
//		{
//			additionalRef[0] = min == Float.MAX_VALUE ? "-" : NumberUtilsKt.formatWhole(min);
//			additionalRef[1] = max == Float.MIN_VALUE ? "-" : NumberUtilsKt.formatWhole(max);
//			additionalRef[2] = NumberUtilsKt.formatWhole(totalIn / (double)inputVals.size());
//		}
	}

	/**
	 * Generates and sets the ppm watering data from the given plant
	 *
	 * @param plant The plant
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setTdsData(Plant plant, @Nullable Context context, @Nullable LineChart chart, String[] additionalRef, TdsUnit selectedUnit)
	{
//		ArrayList<Entry> vals = new ArrayList<>();
//		ArrayList<String> xVals = new ArrayList<>();
//		LineData data = new LineData();
//		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();
//
//		float min = Float.MAX_VALUE;
//		float max = Float.MIN_VALUE;
//		float total = 0f;
//
//		int index = 0;
//		for (Action action : plant.getActions())
//		{
//			if (action instanceof Water && ((Water)action).getTds() != null && ((Water)action).getTds().getType() == selectedUnit)
//			{
//				float value = ((Water)action).getTds().getAmount().floatValue();
//
//				vals.add(new Entry(value, index++));
//				PlantStage stage = null;
//				long changeDate = 0;
//				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
//				while (iterator.hasPrevious())
//				{
//					PlantStage key = iterator.previous();
//					Action changeAction = stageTimes.get(key);
//					if (action.getDate() > changeAction.getDate())
//					{
//						stage = key;
//						changeDate = changeAction.getDate();
//					}
//				}
//
//				long difference = action.getDate() - changeDate;
//				if (stage != null)
//				{
//					xVals.add(((int)TimeHelper.toDays(difference) + "" + (context != null ? context.getString(stage.getPrintString()) : stage.getEnString()).charAt(0)).toLowerCase());
//				}
//				else
//				{
//					xVals.add("");
//				}
//
//				min = Math.min(min, value);
//				max = Math.max(max, value);
//				total += value;
//			}
//		}
//
//		if (chart != null && context != null)
//		{
//			LineDataSet dataSet = new LineDataSet(vals, selectedUnit.getLabel());
//			styleDataset(context, dataSet, Color.parseColor(context.getResources().getStringArray(R.array.stats_colours)[0]));
//			styleGraph(chart);
//
//			chart.getAxisLeft().setValueFormatter(new YAxisValueFormatter()
//			{
//				@Override public String getFormattedValue(float value, YAxis yAxis)
//				{
//					return NumberUtilsKt.formatWhole(value);
//				}
//			});
//			dataSet.setValueFormatter(new ValueFormatter()
//			{
//				@Override public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler)
//				{
//					return NumberUtilsKt.formatWhole(value);
//				}
//			});
//
//			chart.setData(new LineData(xVals, dataSet));
//		}
//
//		if (additionalRef != null)
//		{
//			additionalRef[0] = min == Float.MAX_VALUE ? "-" : NumberUtilsKt.formatWhole(min);
//			additionalRef[1] = max == Float.MIN_VALUE ? "-" : NumberUtilsKt.formatWhole(max);
//			additionalRef[2] = NumberUtilsKt.formatWhole(total / (double)vals.size());
//		}
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
		final TempUnit tempUnit = TempUnit.getSelectedTemperatureUnit(context);
		setTempData(plant, context, tempUnit, chart, additionalRef);
	}

	public static void setTempData(Plant plant, @Nullable Context context, TempUnit tempUnit, @Nullable LineChart chart, String[] additionalRef)
	{
//		ArrayList<Entry> vals = new ArrayList<>();
//		ArrayList<String> xVals = new ArrayList<>();
//		LineData data = new LineData();
//		LinkedHashMap<PlantStage, Action> stageTimes = plant.getStages();
//		float min = Float.MAX_VALUE;
//		float max = Float.MIN_VALUE;
//		float total = 0;
//
//		int index = 0;
//		for (Action action : plant.getActions())
//		{
//			if (action instanceof Water && ((Water)action).getTemp() != null)
//			{
//				float temperature = (float)TempUnit.CELCIUS.to(tempUnit, ((Water)action).getTemp());
//
//				vals.add(new Entry(temperature, index++));
//				PlantStage stage = null;
//				long changeDate = 0;
//				ListIterator<PlantStage> iterator = new ArrayList(stageTimes.keySet()).listIterator(stageTimes.size());
//				while (iterator.hasPrevious())
//				{
//					PlantStage key = iterator.previous();
//					Action changeAction = stageTimes.get(key);
//					if (action.getDate() > changeAction.getDate())
//					{
//						stage = key;
//						changeDate = changeAction.getDate();
//					}
//				}
//
//				long difference = action.getDate() - changeDate;
//				if (stage != null)
//				{
//					xVals.add(((int)TimeHelper.toDays(difference) + "" + (context != null ? context.getString(stage.getPrintString()) : stage.getEnString()).charAt(0)).toLowerCase());
//				}
//				else
//				{
//					xVals.add("");
//				}
//
//				min = Math.min(min, temperature);
//				max = Math.max(max, temperature);
//				total += temperature;
//			}
//		}
//
//		if (chart != null)
//		{
//			LineDataSet dataSet = new LineDataSet(vals, context.getString(R.string.stat_temerature));
//			styleDataset(context, dataSet, Color.parseColor(context.getResources().getStringArray(R.array.stats_colours)[0]));
//
//			LineData lineData = new LineData(xVals, dataSet);
//			lineData.setValueFormatter(formatter);
//
//			chart.getAxisLeft().setAxisMinValue(min - 5f);
//			chart.getAxisLeft().setAxisMaxValue(max + 5f);
//			styleGraph(chart);
//			chart.setData(lineData);
//		}
//
//		if (additionalRef != null)
//		{
//			additionalRef[0] = min == Float.MAX_VALUE ? "-" : NumberUtilsKt.formatWhole((int)min);
//			additionalRef[1] = max == Float.MIN_VALUE ? "-" : NumberUtilsKt.formatWhole((int)max);
//			additionalRef[2] = NumberUtilsKt.formatWhole((int)(total / (double)vals.size()));
//		}
	}

	/**
	 * Generates and sets the temperature data from the given Garden
	 *
	 * @param garden The garden
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setTempData(Garden garden, @NonNull Context context, @Nullable LineChart chart, String[] additionalRef)
	{
		final TempUnit tempUnit = TempUnit.getSelectedTemperatureUnit(context);
		setTempData(garden, context, tempUnit, chart, additionalRef);
	}

	/**
	 * Generates and sets the temperature data from the given Garden
	 *
	 * @param garden The garden
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setTempData(Garden garden, @Nullable Context context, TempUnit tempUnit, @Nullable LineChart chart, String[] additionalRef)
	{
		ArrayList<Entry> vals = new ArrayList<>();
		final ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		float total = 0;

		int index = 0;
		DateFormat dateFormat = null;

		if (context != null)
		{
			dateFormat = android.text.format.DateFormat.getDateFormat(context);
		}

		for (Action action : garden.getActions())
		{
			if (action instanceof TemperatureChange)
			{
				String date = "";

				if (dateFormat != null)
				{
					date = dateFormat.format(new Date(action.getDate()));
				}
				else
				{
					date = DateTimeUtils.toLocalDateTime(new Timestamp(action.getDate())).format(DateTimeFormatter.ofPattern("yyyy/mm/dd"));
				}

				double temperature = TempUnit.CELCIUS.to(tempUnit, ((TemperatureChange)action).getTemp());

				Entry entry = new Entry(index++, (float)temperature);
				entry.setData(action);
				vals.add(entry);
				xVals.add(date);

				min = (float)Math.min(min, temperature);
				max = (float)Math.max(max, temperature);
				total += temperature;
			}
		}

		if (chart != null)
		{
			EntryExtKt.style(chart);
			LineDataSet dataSet = new LineDataSet(vals, context.getString(R.string.stat_temerature));
			styleDataset(context, dataSet, Color.parseColor(context.getResources().getStringArray(R.array.stats_colours)[0]));

			LineData lineData = new LineData(dataSet);

			lineData.setValueFormatter(new ValueFormatter()
			{
				@Override public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler)
				{
					return formatter.getFormattedValue(value, entry, dataSetIndex, viewPortHandler) + "°" + tempUnit.getLabel();
				}
			});

			styleGraph(chart);
			chart.setData(lineData);

			chart.getXAxis().setYOffset(15.0f);
			chart.setExtraOffsets(0, 0, 30, 0);
			chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xVals));
		}

		if (additionalRef != null)
		{
			additionalRef[0] = min == Float.MAX_VALUE ? "-" : NumberUtilsKt.formatWhole(min);
			additionalRef[1] = max == Float.MIN_VALUE ? "-" : NumberUtilsKt.formatWhole(max);
			additionalRef[2] = NumberUtilsKt.formatWhole(total / (double)vals.size());
		}
	}

	/**
	 * Generates and sets the humidity data from the given Garden
	 *
	 * @param garden The garden
	 * @param chart The chart to set the data
	 * @param additionalRef Pass-by-reference value for min/max/ave for the generated values. Must be length of 3 if not null
	 */
	public static void setHumidityData(Garden garden, @Nullable Context context, @Nullable LineChart chart, String[] additionalRef)
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		float total = 0;

		int index = 0;
		DateFormat dateFormat = null;

		if (context != null)
		{
			dateFormat = android.text.format.DateFormat.getDateFormat(context);
		}

		for (Action action : garden.getActions())
		{
			if (action instanceof HumidityChange)
			{
				String date = "";

				if (dateFormat != null)
				{
					date = dateFormat.format(new Date(action.getDate()));
				}
				else
				{
					date = DateTimeUtils.toLocalDateTime(new Timestamp(action.getDate())).format(DateTimeFormatter.ofPattern("yyyy/mm/dd"));
				}

				double humidity = ((HumidityChange)action).getHumidity();

				Entry entry = new Entry(index++, (float)humidity);
				entry.setData(action);
				vals.add(entry);
				xVals.add(date);

				min = (float)Math.min(min, humidity);
				max = (float)Math.max(max, humidity);
				total += humidity;
			}
		}

		if (chart != null && context != null)
		{
			EntryExtKt.style(chart);
			LineDataSet dataSet = new LineDataSet(vals, "%");
			styleDataset(context, dataSet, Color.parseColor(context.getResources().getStringArray(R.array.stats_colours)[2]));

			LineData lineData = new LineData(dataSet);
			lineData.setValueFormatter(new ValueFormatter()
			{
				@Override public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler)
				{
					return formatter.getFormattedValue(value, entry, dataSetIndex, viewPortHandler) + "%";
				}
			});

			chart.getAxisLeft().setAxisMinValue(min - 5f);
			chart.getAxisLeft().setAxisMaxValue(max + 5f);
			styleGraph(chart);
			chart.setData(lineData);

			chart.getXAxis().setYOffset(15.0f);
			chart.setExtraOffsets(0, 0, 30, 0);
			chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xVals));
		}

		if (additionalRef != null)
		{
			additionalRef[0] = min == Float.MAX_VALUE ? "-" : NumberUtilsKt.formatWhole((int)min);
			additionalRef[1] = max == Float.MIN_VALUE ? "-" : NumberUtilsKt.formatWhole((int)max);
			additionalRef[2] = NumberUtilsKt.formatWhole((int)(total / (double)vals.size()));
		}
	}
}
