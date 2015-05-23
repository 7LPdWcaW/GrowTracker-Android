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
import me.anon.model.Feed;
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

	@Views.InjectView(R.id.runoff) private LineChartView runoff;
	@Views.InjectView(R.id.ppm) private LineChartView ppm;
	@Views.InjectView(R.id.nutrients) private LineChartView nutrients;

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
		LineSet nPcSet = new LineSet();

		nPcSet.setDots(true);
		nPcSet.setDotsColor(0xffffffff);
		nPcSet.setLineColor(0xff00ACC1);
		nPcSet.setDotsRadius(12.0f);
		nPcSet.setDotsStrokeThickness(6.0f);
		nPcSet.setDotsStrokeColor(0xff26C6DA);
		nPcSet.setLineThickness(10.0f);

		LineSet pPcSet = new LineSet();
		pPcSet.setDots(true);
		pPcSet.setDotsColor(0xffffffff);
		pPcSet.setLineColor(0xff6D4C41);
		pPcSet.setDotsRadius(12.0f);
		pPcSet.setDotsStrokeThickness(6.0f);
		pPcSet.setDotsStrokeColor(0xff8D6E63);
		pPcSet.setLineThickness(10.0f);

		LineSet kPcSet = new LineSet();
		kPcSet.setDots(true);
		kPcSet.setDotsColor(0xffffffff);
		kPcSet.setLineColor(0xff6D4C41); // 600
		kPcSet.setDotsRadius(12.0f);
		kPcSet.setDotsStrokeThickness(6.0f);
		kPcSet.setDotsStrokeColor(0xff8D6E63); // 400
		kPcSet.setLineThickness(10.0f);

		LineSet caPcSet = new LineSet();
		caPcSet.setDots(true);
		caPcSet.setDotsColor(0xffffffff);
		caPcSet.setLineColor(0xffFB8C00);
		caPcSet.setDotsRadius(12.0f);
		caPcSet.setDotsStrokeThickness(6.0f);
		caPcSet.setDotsStrokeColor(0xffFFA726);
		caPcSet.setLineThickness(10.0f);

		LineSet sPcSet = new LineSet();
		sPcSet.setDots(true);
		sPcSet.setDotsColor(0xffffffff);
		sPcSet.setLineColor(0xff7CB342);
		sPcSet.setDotsRadius(12.0f);
		sPcSet.setDotsStrokeThickness(6.0f);
		sPcSet.setDotsStrokeColor(0xff9CCC65);
		sPcSet.setLineThickness(10.0f);

		LineSet mgPcSet = new LineSet();
		mgPcSet.setDots(true);
		mgPcSet.setDotsColor(0xffffffff);
		mgPcSet.setLineColor(0xff00ACC1);
		mgPcSet.setDotsRadius(12.0f);
		mgPcSet.setDotsStrokeThickness(6.0f);
		mgPcSet.setDotsStrokeColor(0xff26C6DA);
		mgPcSet.setLineThickness(10.0f);

		for (Action action : plant.getActions())
		{
			if (action instanceof Feed && ((Feed)action).getNutrient() != null)
			{
				nPcSet.addPoint(new Point("", ((Feed)action).getNutrient().getNpc() == null ? 0 : ((Feed)action).getNutrient().getNpc().floatValue()));
				pPcSet.addPoint(new Point("", ((Feed)action).getNutrient().getPpc() == null ? 0 : ((Feed)action).getNutrient().getPpc().floatValue()));
				kPcSet.addPoint(new Point("", ((Feed)action).getNutrient().getKpc() == null ? 0 : ((Feed)action).getNutrient().getKpc().floatValue()));

				caPcSet.addPoint(new Point("", ((Feed)action).getNutrient().getCapc() == null ? 0 : ((Feed)action).getNutrient().getCapc().floatValue()));
				sPcSet.addPoint(new Point("", ((Feed)action).getNutrient().getSpc() == null ? 0 : ((Feed)action).getNutrient().getSpc().floatValue()));
				mgPcSet.addPoint(new Point("", ((Feed)action).getNutrient().getMgpc() == null ? 0 : ((Feed)action).getNutrient().getMgpc().floatValue()));
			}
		}

		nutrients.setXAxis(false);
		nutrients.setXLabels(XController.LabelPosition.OUTSIDE);
		nutrients.setYAxis(false);
		nutrients.setYLabels(YController.LabelPosition.OUTSIDE);
		nutrients.setBackgroundColor(0xff1A237E);
		nutrients.setLabelColor(0xffffffff);
		nutrients.addData(nPcSet);
		nutrients.addData(pPcSet);
		nutrients.show();
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
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getRunoff() != null)
			{
				set.addPoint(new Point("", ((Water)action).getRunoff().floatValue()));
				min = Math.min(min, ((Water)action).getRunoff().floatValue());
				max = Math.max(max, ((Water)action).getRunoff().floatValue());
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
		runoff.setAxisBorderValues((int)Math.max(min, 0), (int)max + 1, 1);
		runoff.addData(set);
		runoff.show();
	}
}
