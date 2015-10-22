package me.anon.grow.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.helper.TimeHelper;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Feed;
import me.anon.model.Plant;
import me.anon.model.PlantMedium;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;

/**
 * // TODO: Add class description
 *
 * TODO: Average time between feeds
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class StatisticsFragment extends Fragment
{
	private int plantIndex = -1;
	private Plant plant;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static StatisticsFragment newInstance(int plantIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);

		StatisticsFragment fragment = new StatisticsFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Views.InjectView(R.id.input_ph) private LineChart inputPh;
	@Views.InjectView(R.id.runoff) private LineChart runoff;
	@Views.InjectView(R.id.ppm) private LineChart ppm;
	@Views.InjectView(R.id.temp) private LineChart temp;

	@Views.InjectView(R.id.grow_time) private TextView growTime;
	@Views.InjectView(R.id.feed_count) private TextView feedCount;
	@Views.InjectView(R.id.water_count) private TextView waterCount;
	@Views.InjectView(R.id.flush_count) private TextView flushCount;

	@Views.InjectView(R.id.germ_time) private TextView germTime;
	@Views.InjectView(R.id.germ_time_container) private View germTimeContainer;
	@Views.InjectView(R.id.veg_time) private TextView vegTime;
	@Views.InjectView(R.id.veg_time_container) private View vegTimeContainer;
	@Views.InjectView(R.id.flower_time) private TextView flowerTime;
	@Views.InjectView(R.id.flower_time_container) private View flowerTimeContainer;
	@Views.InjectView(R.id.dry_time) private TextView dryTime;
	@Views.InjectView(R.id.dry_time_container) private View dryTimeContainer;
	@Views.InjectView(R.id.cure_time) private TextView cureTime;
	@Views.InjectView(R.id.cure_time_container) private View cureTimeContainer;

	@Views.InjectView(R.id.ave_feed) private TextView aveFeed;
	@Views.InjectView(R.id.ave_water) private TextView aveWater;

	@Views.InjectView(R.id.min_ph) private TextView minph;
	@Views.InjectView(R.id.max_ph) private TextView maxph;
	@Views.InjectView(R.id.ave_ph) private TextView aveph;

	@Views.InjectView(R.id.min_input_ph) private TextView minInputPh;
	@Views.InjectView(R.id.max_input_ph) private TextView maxInputPh;
	@Views.InjectView(R.id.ave_input_ph) private TextView aveInputPh;

	@Views.InjectView(R.id.min_ppm) private TextView minppm;
	@Views.InjectView(R.id.max_ppm) private TextView maxppm;
	@Views.InjectView(R.id.ave_ppm) private TextView aveppm;

	@Views.InjectView(R.id.temp_container) private View tempContainer;
	@Views.InjectView(R.id.min_temp) private TextView mintemp;
	@Views.InjectView(R.id.max_temp) private TextView maxtemp;
	@Views.InjectView(R.id.ave_temp) private TextView avetemp;

	private ValueFormatter formatter = new ValueFormatter()
	{
		@Override public String getFormattedValue(float value)
		{
			return String.format("%.2f", value);
		}
	};

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.statistics_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle("Plant statistics");

		if (getArguments() != null)
		{
			plantIndex = getArguments().getInt("plant_index");

			if (plantIndex > -1)
			{
				plant = PlantManager.getInstance().getPlants().get(plantIndex);
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		}

		setStatistics();
		setInput();
		setRunoff();
		setPpm();
		setTemp();
	}

	private void setStatistics()
	{
		long startDate = plant.getPlantDate();
		long endDate = System.currentTimeMillis();
		long feedDifference = 0L;
		long waterDifference = 0L;
		long lastFeed = 0L, lastWater = 0L;
		int totalFeed = 0, totalWater = 0, totalFlush = 0;
		SortedMap<PlantStage, Long> stages = new TreeMap<PlantStage, Long>(new Comparator<PlantStage>()
		{
			@Override public int compare(PlantStage lhs, PlantStage rhs)
			{
				if (lhs.ordinal() < rhs.ordinal())
				{
					return 1;
				}
				else if (lhs.ordinal() > rhs.ordinal())
				{
					return -1;
				}

				return 0;
			}
		});

		for (Action action : plant.getActions())
		{
			if (action instanceof StageChange)
			{
				stages.put(((StageChange)action).getNewStage(), action.getDate());

				if (((StageChange)action).getNewStage() == PlantStage.HARVESTED)
				{
					endDate = action.getDate();
				}
			}

			if (action instanceof Feed)
			{
				if (lastFeed != 0)
				{
					feedDifference += Math.abs(action.getDate() - lastFeed);
				}

				totalFeed++;
				lastFeed = action.getDate();

			}
			else if (action instanceof Water)
			{
				if (lastWater != 0)
				{
					waterDifference += Math.abs(action.getDate() - lastWater);
				}

				totalWater++;
				lastWater = action.getDate();
			}
			else if (action instanceof EmptyAction && ((EmptyAction)action).getAction() == Action.ActionName.FLUSH)
			{
				totalFlush++;
			}
		}

		int stageIndex = 0;
		long lastStage = 0;
		PlantStage previous = stages.firstKey();
		for (PlantStage plantStage : stages.keySet())
		{
			long difference = 0;
			if (stageIndex == 0)
			{
				difference = endDate - stages.get(plantStage);
			}
			else
			{
				difference = lastStage - stages.get(plantStage);
			}

			previous = plantStage;
			lastStage = stages.get(plantStage);
			stageIndex++;

			stages.put(plantStage, difference);
		}

		long seconds = ((endDate - startDate) / 1000);
		double days = (double)seconds * 0.0000115741d;

		growTime.setText(String.format("%1$,.2f", days) + " days");
		feedCount.setText(String.valueOf(totalFeed));
		waterCount.setText(String.valueOf(totalWater));
		flushCount.setText(String.valueOf(totalFlush));
		aveFeed.setText(String.format("%1$,.2f", (TimeHelper.toDays(feedDifference) / (double)totalFeed)) + " days");
		aveWater.setText(String.format("%1$,.2f", (TimeHelper.toDays(waterDifference) / (double)totalWater)) + " days");

		if (stages.containsKey(PlantStage.GERMINATION))
		{
			germTime.setText((int)TimeHelper.toDays(stages.get(PlantStage.GERMINATION)) + " days");
			germTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.VEGETATION))
		{
			vegTime.setText((int)TimeHelper.toDays(stages.get(PlantStage.VEGETATION)) + " days");
			vegTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.FLOWER))
		{
			flowerTime.setText((int)TimeHelper.toDays(stages.get(PlantStage.FLOWER)) + " days");
			flowerTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.DRYING))
		{
			dryTime.setText((int)TimeHelper.toDays(stages.get(PlantStage.DRYING)) + " days");
			dryTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.CURING))
		{
			cureTime.setText((int)TimeHelper.toDays(stages.get(PlantStage.CURING)) + " days");
			cureTimeContainer.setVisibility(View.VISIBLE);
		}
	}

	private void setPpm()
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

		minppm.setText(String.valueOf((int)min));
		maxppm.setText(String.valueOf((int)max));
		aveppm.setText(String.valueOf((int)(ave / (double)index)));

		LineDataSet dataSet = new LineDataSet(vals, "PPM");
		dataSet.setDrawCubic(true);
		dataSet.setLineWidth(2.0f);
		dataSet.setDrawCircleHole(false);
		dataSet.setCircleColor(0xffffffff);
		dataSet.setValueTextColor(0xffffffff);
		dataSet.setCircleSize(5.0f);
		dataSet.setValueTextSize(8.0f);
		dataSet.setColor(0xffA7FFEB);

		ppm.setBackgroundColor(0xff1B5E20);
		ppm.setGridBackgroundColor(0xff1B5E20);
		ppm.setDrawGridBackground(false);
		ppm.setHighlightEnabled(false);
		ppm.getLegend().setEnabled(false);
		ppm.getAxisLeft().setTextColor(0xffffffff);
		ppm.getAxisRight().setEnabled(false);
		ppm.getAxisLeft().setXOffset(8.0f);
		ppm.setScaleYEnabled(false);
		ppm.setDescription("");
		ppm.setPinchZoom(false);
		ppm.setDoubleTapToZoomEnabled(false);
		ppm.setData(new LineData(xVals, dataSet));
	}

	private void setInput()
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		float min = 14f;
		float max = -14f;
		float ave = 0;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getPh() != null)
			{
				vals.add(new Entry(((Water)action).getPh().floatValue(), index++));
				xVals.add("");

				min = Math.min(min, ((Water)action).getPh().floatValue());
				max = Math.max(max, ((Water)action).getPh().floatValue());
				ave += ((Water)action).getPh().floatValue();
			}
		}

		minInputPh.setText(String.valueOf(min));
		maxInputPh.setText(String.valueOf(max));
		aveInputPh.setText(String.format("%1$,.2f", (ave / (double)index)));

		LineDataSet dataSet = new LineDataSet(vals, "Input PH");
		dataSet.setDrawCubic(true);
		dataSet.setLineWidth(2.0f);
		dataSet.setDrawCircleHole(false);
		dataSet.setCircleColor(0xffffffff);
		dataSet.setValueTextColor(0xffffffff);
		dataSet.setCircleSize(5.0f);
		dataSet.setValueTextSize(8.0f);
		dataSet.setValueFormatter(formatter);

		LineData lineData = new LineData(xVals, dataSet);
		lineData.setValueFormatter(formatter);

		inputPh.setBackgroundColor(0xff006064);
		inputPh.setGridBackgroundColor(0xff006064);
		inputPh.setDrawGridBackground(false);
		inputPh.setHighlightEnabled(false);
		inputPh.getLegend().setEnabled(false);
		inputPh.getAxisLeft().setTextColor(0xffffffff);
		inputPh.getAxisRight().setEnabled(false);
		inputPh.getAxisLeft().setValueFormatter(formatter);
		inputPh.getAxisLeft().setXOffset(8.0f);
		inputPh.getAxisLeft().setAxisMinValue(min - 0.5f);
		inputPh.getAxisLeft().setAxisMaxValue(max + 0.5f);
		inputPh.getAxisLeft().setStartAtZero(false);
		inputPh.setScaleYEnabled(false);
		inputPh.setDescription("");
		inputPh.setPinchZoom(false);
		inputPh.setDoubleTapToZoomEnabled(false);
		inputPh.setData(lineData);
	}

	private void setRunoff()
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		float min = 14f;
		float max = -14f;
		float ave = 0;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getRunoff() != null)
			{
				vals.add(new Entry(((Water)action).getRunoff().floatValue(), index++));
				xVals.add("");

				min = Math.min(min, ((Water)action).getRunoff().floatValue());
				max = Math.max(max, ((Water)action).getRunoff().floatValue());
				ave += ((Water)action).getRunoff().floatValue();
			}
		}

		minph.setText(String.valueOf(min));
		maxph.setText(String.valueOf(max));
		aveph.setText(String.format("%1$,.2f", (ave / (double)index)));

		LineDataSet dataSet = new LineDataSet(vals, "Runoff PH");
		dataSet.setDrawCubic(true);
		dataSet.setLineWidth(2.0f);
		dataSet.setDrawCircleHole(false);
		dataSet.setCircleColor(0xffffffff);
		dataSet.setValueTextColor(0xffffffff);
		dataSet.setCircleSize(5.0f);
		dataSet.setValueTextSize(8.0f);
		dataSet.setValueFormatter(formatter);

		LineData lineData = new LineData(xVals, dataSet);
		lineData.setValueFormatter(formatter);

		runoff.setBackgroundColor(0xff01579B);
		runoff.setGridBackgroundColor(0xff01579B);
		runoff.setDrawGridBackground(false);
		runoff.setHighlightEnabled(false);
		runoff.getLegend().setEnabled(false);
		runoff.getAxisLeft().setTextColor(0xffffffff);
		runoff.getAxisRight().setEnabled(false);
		runoff.getAxisLeft().setValueFormatter(formatter);
		runoff.getAxisLeft().setXOffset(8.0f);
		runoff.getAxisLeft().setAxisMinValue(min - 0.5f);
		runoff.getAxisLeft().setAxisMaxValue(max + 0.5f);
		runoff.getAxisLeft().setStartAtZero(false);
		runoff.setScaleYEnabled(false);
		runoff.setDescription("");
		runoff.setPinchZoom(false);
		runoff.setDoubleTapToZoomEnabled(false);
		runoff.setData(lineData);
	}

	private void setTemp()
	{
		if (plant.getMedium() == PlantMedium.HYDRO)
		{
			tempContainer.setVisibility(View.VISIBLE);

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

			mintemp.setText(String.valueOf(min));
			maxtemp.setText(String.valueOf(max));
			avetemp.setText(String.format("%1$,.2f", (ave / (double)index)));

			LineDataSet dataSet = new LineDataSet(vals, "Temperature");
			dataSet.setDrawCubic(true);
			dataSet.setLineWidth(2.0f);
			dataSet.setDrawCircleHole(false);
			dataSet.setCircleColor(0xffffffff);
			dataSet.setValueTextColor(0xffffffff);
			dataSet.setCircleSize(5.0f);
			dataSet.setValueTextSize(8.0f);
			dataSet.setValueFormatter(formatter);

			LineData lineData = new LineData(xVals, dataSet);
			lineData.setValueFormatter(formatter);

			temp.setBackgroundColor(0xff311B92);
			temp.setGridBackgroundColor(0xff311B92);
			temp.setDrawGridBackground(false);
			temp.setHighlightEnabled(false);
			temp.getLegend().setEnabled(false);
			temp.getAxisLeft().setTextColor(0xffffffff);
			temp.getAxisRight().setEnabled(false);
			temp.getAxisLeft().setValueFormatter(formatter);
			temp.getAxisLeft().setXOffset(8.0f);
			temp.getAxisLeft().setAxisMinValue(min - 5f);
			temp.getAxisLeft().setAxisMaxValue(max + 5f);
			temp.getAxisLeft().setStartAtZero(false);
			temp.setScaleYEnabled(false);
			temp.setDescription("");
			temp.setPinchZoom(false);
			temp.setDoubleTapToZoomEnabled(false);
			temp.setData(lineData);
		}
	}
}
