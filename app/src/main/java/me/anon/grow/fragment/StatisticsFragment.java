package me.anon.grow.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Plant;
import me.anon.model.Water;

/**
 * // TODO: Add class description
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

	@Views.InjectView(R.id.runoff) private LineChartView runoff;
	@Views.InjectView(R.id.ppm) private LineChartView ppm;

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
	}

	private void setPpm()
	{
		LineSet set = new LineSet();
		long maxValue = Long.MIN_VALUE;

		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getPpm() != null)
			{
				set.addPoint(new Point("", ((Water)action).getPpm().floatValue()));
				maxValue = Math.max(maxValue, ((Water)action).getPpm().longValue());
			}
		}

		set.setDots(true);
		set.setDotsColor(0xffffffff);
		set.setLineColor(0xff00ACC1);
		set.setDotsRadius(12.0f);
		set.setDotsStrokeThickness(6.0f);
		set.setDotsStrokeColor(0xff26C6DA);
		set.setLineThickness(10.0f);
		set.setFill(0xff0097A7);

		ppm.setXAxis(false);
		ppm.setXLabels(XController.LabelPosition.OUTSIDE);
		ppm.setYAxis(false);
		ppm.setYLabels(YController.LabelPosition.OUTSIDE);
		ppm.setBackgroundColor(0xff006064);
		ppm.setLabelColor(0xffffffff);
		ppm.setAxisBorderValues(0, (int)Math.max(maxValue, 700) + 100, 350);
		ppm.addData(set);
		ppm.show();
	}

	private void setRunoff()
	{
		LineSet set = new LineSet();

		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getRunoff() != null)
			{
				set.addPoint(new Point("", ((Water)action).getRunoff().floatValue()));
			}
		}

		set.setDots(true);
		set.setDotsColor(0xffffffff);
		set.setLineColor(0xff039BE5);
		set.setDotsRadius(12.0f);
		set.setDotsStrokeThickness(6.0f);
		set.setDotsStrokeColor(0xff29B6F6);
		set.setLineThickness(10.0f);
		set.setFill(0xff0288D1);

		runoff.setXAxis(false);
		runoff.setXLabels(XController.LabelPosition.OUTSIDE);
		runoff.setYAxis(false);
		runoff.setYLabels(YController.LabelPosition.OUTSIDE);
		runoff.setBackgroundColor(0xff01579B);
		runoff.setLabelColor(0xffffffff);
		runoff.setAxisBorderValues(0, 14, 1);
		runoff.addData(set);
		runoff.show();
	}
}
