package me.anon.grow.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.anon.grow.R;
import me.anon.lib.TdsUnit;
import me.anon.lib.TempUnit;
import me.anon.lib.Views;
import me.anon.lib.ext.IntUtilsKt;
import me.anon.lib.helper.StatsHelper;
import me.anon.lib.helper.TimeHelper;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.EmptyAction;
import me.anon.model.Plant;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;

@Views.Injectable
public class StatisticsFragment extends Fragment
{
	private Plant plant;

	public static StatisticsFragment newInstance(Bundle args)
	{
		StatisticsFragment fragment = new StatisticsFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Views.InjectView(R.id.additives) private LineChart additives;
	@Views.InjectView(R.id.additive_selector) private TextView additivesSpinner;
	@Views.InjectView(R.id.input_ph) private LineChart inputPh;
	@Views.InjectView(R.id.tds_selector) private TextView tdsSpinner;
	@Views.InjectView(R.id.tds) private LineChart tds;
	@Views.InjectView(R.id.temp) private LineChart temp;
	@Views.InjectView(R.id.stage_chart) private BarChart stagesChart;

	@Views.InjectView(R.id.grow_time) private TextView growTime;
	@Views.InjectView(R.id.water_count) private TextView waterCount;
	@Views.InjectView(R.id.flush_count) private TextView flushCount;

	@Views.InjectView(R.id.germ_time) private TextView germTime;
	@Views.InjectView(R.id.germ_time_container) private View germTimeContainer;
	@Views.InjectView(R.id.veg_time) private TextView vegTime;
	@Views.InjectView(R.id.veg_time_container) private View vegTimeContainer;
	@Views.InjectView(R.id.seedling_time) private TextView seedlingTime;
	@Views.InjectView(R.id.seedling_time_container) private View seedlingTimeContainer;
	@Views.InjectView(R.id.cutting_time) private TextView cuttingTime;
	@Views.InjectView(R.id.cutting_time_container) private View cuttingTimeContainer;
	@Views.InjectView(R.id.flower_time) private TextView flowerTime;
	@Views.InjectView(R.id.flower_time_container) private View flowerTimeContainer;
	@Views.InjectView(R.id.dry_time) private TextView dryTime;
	@Views.InjectView(R.id.dry_time_container) private View dryTimeContainer;
	@Views.InjectView(R.id.cure_time) private TextView cureTime;
	@Views.InjectView(R.id.cure_time_container) private View cureTimeContainer;

	@Views.InjectView(R.id.ave_water) private TextView aveWater;

	@Views.InjectView(R.id.min_input_ph) private TextView minInputPh;
	@Views.InjectView(R.id.max_input_ph) private TextView maxInputPh;
	@Views.InjectView(R.id.ave_input_ph) private TextView aveInputPh;

	@Views.InjectView(R.id.tds_title) private TextView ppmTitle;
	@Views.InjectView(R.id.min_tds) private TextView mintds;
	@Views.InjectView(R.id.max_tds) private TextView maxtds;
	@Views.InjectView(R.id.ave_tds) private TextView avetds;
	private TdsUnit selectedTdsUnit;

	@Views.InjectView(R.id.temp_container) private View tempContainer;
	@Views.InjectView(R.id.min_temp) private TextView mintemp;
	@Views.InjectView(R.id.max_temp) private TextView maxtemp;
	@Views.InjectView(R.id.ave_temp) private TextView avetemp;

	private Set<String> checkedAdditives = null;

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.statistics_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null)
		{
			checkedAdditives = new HashSet<>(savedInstanceState.getStringArrayList("checked_additives"));
		}

		getActivity().setTitle(R.string.statistics_title);

		if (getArguments() != null)
		{
			plant = getArguments().getParcelable("plant");
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}

		selectedTdsUnit = TdsUnit.getSelectedTdsUnit(getActivity());
		setStatistics();

		String[] inputAdditional = new String[3];
		StatsHelper.setInputData(plant, getActivity(), inputPh, inputAdditional);
		minInputPh.setText(inputAdditional[0].equals(String.valueOf(Float.MAX_VALUE)) ? "0" : inputAdditional[0]);
		maxInputPh.setText(inputAdditional[1].equals(String.valueOf(Float.MIN_VALUE)) ? "0" : inputAdditional[1]);
		aveInputPh.setText(inputAdditional[2]);

		setTdsStats();
		setAdditiveStats();

		tempContainer.setVisibility(View.VISIBLE);

		TempUnit tempUnit = TempUnit.getSelectedTemperatureUnit(getActivity());
		String[] tempAdditional = new String[3];
		StatsHelper.setTempData(plant, getActivity(), temp, tempAdditional);
		mintemp.setText(tempAdditional[0].equals("100") ? "-" : tempAdditional[0] + "°" + tempUnit.getLabel());
		maxtemp.setText(tempAdditional[1].equals("-100") ? "-" : tempAdditional[1] + "°" + tempUnit.getLabel());
		avetemp.setText(tempAdditional[2] + "°" + tempUnit.getLabel());
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putStringArrayList("checked_additives", new ArrayList<String>(checkedAdditives));
	}

	private void setTdsStats()
	{
		final Set<TdsUnit> tdsNames = new TreeSet<>();
		for (Action action : plant.getActions())
		{
			if (action instanceof Water && ((Water)action).getTds() != null)
			{
				tdsNames.add(((Water)action).getTds().getType());
			}
		}

		tdsSpinner.setText(selectedTdsUnit.getStrRes());
		tdsSpinner.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				PopupMenu menu = new PopupMenu(v.getContext(), v);
				for (TdsUnit tdsName : tdsNames)
				{
					menu.getMenu().add(0, tdsName.ordinal(), 0, tdsName.getStrRes());
				}

				menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{
					@Override public boolean onMenuItemClick(MenuItem item)
					{
						selectedTdsUnit = TdsUnit.values()[item.getItemId()];
						setTdsStats();
						return true;
					}
				});

				menu.show();
			}
		});

		String[] tdsAdditional = new String[3];
		StatsHelper.setTdsData(plant, getActivity(), tds, tdsAdditional, selectedTdsUnit);
		tds.notifyDataSetChanged();
		tds.postInvalidate();
		mintds.setText(tdsAdditional[0].equals(String.valueOf(Long.MAX_VALUE)) ? "0" : tdsAdditional[0]);
		maxtds.setText(tdsAdditional[1].equals(String.valueOf(Long.MIN_VALUE)) ? "0" : tdsAdditional[1]);
		avetds.setText(tdsAdditional[2]);
		ppmTitle.setText(selectedTdsUnit.getStrRes());
	}

	private void setAdditiveStats()
	{
		final Set<String> additiveNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		for (Action action : plant.getActions())
		{
			if (action instanceof Water)
			{
				List<Additive> actionAdditives = ((Water)action).getAdditives();
				for (Additive additive : actionAdditives)
				{
					additiveNames.add(additive.getDescription());
				}
			}
		}

		if (checkedAdditives == null)
		{
			checkedAdditives = new HashSet<>();
			checkedAdditives.addAll(additiveNames);
		}

		StatsHelper.setAdditiveData(plant, getActivity(), additives, checkedAdditives);
		additives.notifyDataSetChanged();
		additives.postInvalidate();

		additivesSpinner.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				PopupMenu menu = new PopupMenu(v.getContext(), v);
				menu.getMenu().add(R.string.all_none).setCheckable(false);
				for (String additiveName : additiveNames)
				{
					menu.getMenu().add(additiveName).setCheckable(true).setChecked(checkedAdditives.contains(additiveName));
				}

				menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{
					@Override public boolean onMenuItemClick(MenuItem item)
					{
						if (!item.isCheckable())
						{
							if (checkedAdditives.size() != additiveNames.size())
							{
								checkedAdditives.clear();
								checkedAdditives.addAll(additiveNames);
							}
							else
							{
								checkedAdditives.clear();
							}
						}
						else
						{
							if (item.isChecked())
							{
								checkedAdditives.remove(item.getTitle().toString());
							}
							else
							{
								checkedAdditives.add(item.getTitle().toString());
							}
						}

						setAdditiveStats();
						return true;
					}
				});

				menu.show();
			}
		});
	}

	private void setStatistics()
	{
		long startDate = plant.getPlantDate();
		long endDate = System.currentTimeMillis();
		long feedDifference = 0L;
		long waterDifference = 0L;
		long lastWater = 0L;
		int totalWater = 0, totalFlush = 0;

		for (Action action : plant.getActions())
		{
			if (action instanceof StageChange)
			{
				if (((StageChange)action).getNewStage() == PlantStage.HARVESTED)
				{
					endDate = action.getDate();
				}
			}

			if (action.getClass() == Water.class)
			{
				if (lastWater != 0)
				{
					waterDifference += Math.abs(action.getDate() - lastWater);
				}

				totalWater++;
				lastWater = action.getDate();
			}

			if (action instanceof EmptyAction && ((EmptyAction)action).getAction() == Action.ActionName.FLUSH)
			{
				totalFlush++;
			}
		}

		long seconds = ((endDate - startDate) / 1000);
		double days = (double)seconds * 0.0000115741d;

		growTime.setText(getString(R.string.length_days, String.format("%1$,.2f", days)));
		waterCount.setText(String.valueOf(totalWater));
		flushCount.setText(String.valueOf(totalFlush));
		aveWater.setText(getString(R.string.length_days, String.format("%1$,.2f", (TimeHelper.toDays(waterDifference) / (double)totalWater))));

		SortedMap<PlantStage, Long> stages = plant.calculateStageTime();

		if (stages.containsKey(PlantStage.GERMINATION))
		{
			germTime.setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(PlantStage.GERMINATION))));
			germTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.VEGETATION))
		{
			vegTime.setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(PlantStage.VEGETATION))));
			vegTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.SEEDLING))
		{
			seedlingTime.setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(PlantStage.SEEDLING))));
			seedlingTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.CUTTING))
		{
			cuttingTime.setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(PlantStage.CUTTING))));
			cuttingTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.FLOWER))
		{
			flowerTime.setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(PlantStage.FLOWER))));
			flowerTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.DRYING))
		{
			dryTime.setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(PlantStage.DRYING))));
			dryTimeContainer.setVisibility(View.VISIBLE);
		}

		if (stages.containsKey(PlantStage.CURING))
		{
			cureTime.setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(PlantStage.CURING))));
			cureTimeContainer.setVisibility(View.VISIBLE);
		}

		stages.remove(PlantStage.HARVESTED);
		ArrayList<BarEntry> entry = new ArrayList<>();
		String[] labels = new String[stages.size()];
		float[] yVals = new float[stages.size()];

		String[] statsHex = getResources().getStringArray(R.array.stats_colours);
		int[] statsColours = new int[statsHex.length];
		for (int index = 0; index < statsHex.length; index++)
		{
			statsColours[index] = Color.parseColor(statsHex[index]);
		}

		int index = stages.size() - 1;

		for (PlantStage plantStage : stages.keySet())
		{
			yVals[index] = Math.max((float)(int)TimeHelper.toDays(stages.get(plantStage)), 1f);
			labels[index--] = getString(plantStage.getPrintString());
		}

		entry.add(new BarEntry(yVals, 0));

		BarDataSet set = new BarDataSet(entry, "");
		set.setColors(statsColours);
		set.setStackLabels(labels);
		set.setValueTextSize(12.0f);
		set.setValueTextColor(IntUtilsKt.resolveColor(R.attr.chart_label, getActivity()));
		set.setHighlightEnabled(false);

		BarData data = new BarData(new String[] { "" }, set);
		data.setValueFormatter(new ValueFormatter()
		{
			@Override public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler)
			{
				return (int)value + getString(R.string.day_abbr);
			}
		});

		StatsHelper.styleGraph(stagesChart);

		stagesChart.getXAxis().setLabelsToSkip(0);
		stagesChart.getAxisLeft().setValueFormatter(new YAxisValueFormatter()
		{
			@Override public String getFormattedValue(float value, YAxis yAxis)
			{
				return "" + (int)value;
			}
		});
		stagesChart.getAxisRight().setValueFormatter(new YAxisValueFormatter()
		{
			@Override public String getFormattedValue(float value, YAxis yAxis)
			{
				return "" + (int)value;
			}
		});
//		stagesChart.setTouchEnabled(false);

		stagesChart.setMarkerView(null);
		stagesChart.setHighlightPerTapEnabled(false);
		stagesChart.getAxisLeft().setStartAtZero(true);
		stagesChart.setData(data);
	}
}
