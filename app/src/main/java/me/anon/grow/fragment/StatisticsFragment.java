package me.anon.grow.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Plant;
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

	@Views.InjectView(R.id.runoff) private LineChart runoff;
	@Views.InjectView(R.id.ppm) private LineChart ppm;
	@Views.InjectView(R.id.nutrients) private LineChart nutrients;

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

		setRunoff();
		setPpm();
		setNutrients();
	}

	private void setNutrients()
	{

	}

	private void setPpm()
	{

	}

	private void setRunoff()
	{
		ArrayList<Entry> vals = new ArrayList<>();
		ArrayList<String> xVals = new ArrayList<>();
		LineData data = new LineData();
		float min = 14f;
		float max = -14f;

		int index = 0;
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getRunoff() != null)
			{
				vals.add(new Entry(((Water)action).getRunoff().floatValue(), index++));
				xVals.add("");

				min = Math.min(min, ((Water)action).getRunoff().floatValue());
				max = Math.max(max, ((Water)action).getRunoff().floatValue());
			}
		}

		LineDataSet dataSet = new LineDataSet(vals, "Runoff PH");
		dataSet.setDrawCubic(true);
		dataSet.setLineWidth(2.0f);
		dataSet.setDrawCircleHole(false);
		dataSet.setCircleColor(0xffffffff);
		dataSet.setValueTextColor(0xffffffff);
		dataSet.setCircleSize(5.0f);
		dataSet.setValueTextSize(8.0f);

		runoff.setBackgroundColor(0xff01579B);
		runoff.setGridBackgroundColor(0xff01579B);
		runoff.setDrawGridBackground(false);

		runoff.setHighlightEnabled(false);
		runoff.getLegend().setEnabled(false);
		runoff.getAxisLeft().setTextColor(0xffffffff);
		runoff.getAxisRight().setEnabled(false);
		runoff.getAxisLeft().setAxisMinValue(min - 0.5f);
		runoff.getAxisLeft().setAxisMaxValue(max + 0.5f);
		runoff.getAxisLeft().setStartAtZero(false);
		runoff.setScaleYEnabled(false);
		runoff.setDescription("");
		runoff.setPinchZoom(false);
		runoff.setDoubleTapToZoomEnabled(false);
		runoff.setData(new LineData(xVals, dataSet));
	}
}
