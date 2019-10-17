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
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.flexbox.FlexboxLayout;

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
import me.anon.lib.Unit;
import me.anon.lib.Views;
import me.anon.lib.ext.IntUtilsKt;
import me.anon.lib.ext.NumberUtilsKt;
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

	@Views.InjectView(R.id.stats_container) private FlexboxLayout statsContainer;

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
		tds.setMarkerView(new MarkerView(getActivity(), R.layout.chart_marker)
		{
			@Override
			public void refreshContent(Entry e, Highlight highlight)
			{
				String val = NumberUtilsKt.formatWhole(e.getVal());

				((TextView)findViewById(R.id.content)).setText(val);
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
		final Unit measurement = Unit.getSelectedMeasurementUnit(getActivity());
		final Unit delivery = Unit.getSelectedDeliveryUnit(getActivity());

		additives.setMarkerView(new MarkerView(getActivity(), R.layout.chart_marker)
		{
			@Override
			public void refreshContent(Entry e, Highlight highlight)
			{
				String val = NumberUtilsKt.formatWhole(e.getVal());

				((TextView)findViewById(R.id.content)).setText(val + measurement.getLabel() + "/" + delivery.getLabel());

				int color = IntUtilsKt.resolveColor(R.attr.colorPrimary, getActivity());
				if (e.getData() instanceof Integer)
				{
					color = (int)e.getData();
				}

				((TextView)findViewById(R.id.content)).setTextColor(color);
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

		SortedMap<PlantStage, Long> stages = plant.calculateStageTime();
		statsContainer.removeAllViews();

		for (PlantStage value : PlantStage.values())
		{
			if (stages.containsKey(value) && value != PlantStage.HARVESTED)
			{
				View dataView = LayoutInflater.from(getActivity()).inflate(R.layout.data_label_stub, statsContainer, false);
				((TextView)dataView.findViewById(R.id.label)).setText(getString(value.getPrintString()) + ":");
				((TextView)dataView.findViewById(R.id.data)).setText(getString(R.string.length_days, "" + (int)TimeHelper.toDays(stages.get(value))));
				statsContainer.addView(dataView);
			}
		}

		long seconds = ((endDate - startDate) / 1000);
		double days = (double)seconds * 0.0000115741d;

		View growTime = LayoutInflater.from(getActivity()).inflate(R.layout.data_label_stub, statsContainer, false);
		((TextView)growTime.findViewById(R.id.label)).setText(R.string.total_time_label);
		((TextView)growTime.findViewById(R.id.data)).setText(getString(R.string.length_days, "" + NumberUtilsKt.formatWhole(days)));
		statsContainer.addView(growTime);

		View waterCount = LayoutInflater.from(getActivity()).inflate(R.layout.data_label_stub, statsContainer, false);
		((TextView)waterCount.findViewById(R.id.label)).setText(R.string.total_waters_label);
		((TextView)waterCount.findViewById(R.id.data)).setText(NumberUtilsKt.formatWhole(totalWater));
		statsContainer.addView(waterCount);

		View flushCount = LayoutInflater.from(getActivity()).inflate(R.layout.data_label_stub, statsContainer, false);
		((TextView)flushCount.findViewById(R.id.label)).setText(R.string.total_flushes_label);
		((TextView)flushCount.findViewById(R.id.data)).setText(NumberUtilsKt.formatWhole(totalFlush));
		statsContainer.addView(flushCount);

		View aveWater = LayoutInflater.from(getActivity()).inflate(R.layout.data_label_stub, statsContainer, false);
		((TextView)aveWater.findViewById(R.id.label)).setText(R.string.ave_time_between_water_label);
		((TextView)aveWater.findViewById(R.id.data)).setText(getString(R.string.length_days, NumberUtilsKt.formatWhole(TimeHelper.toDays(waterDifference) / (double)totalWater)));
		statsContainer.addView(aveWater);

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

		stagesChart.setMarkerView(null);
		stagesChart.setHighlightPerTapEnabled(false);
		stagesChart.getAxisLeft().setStartAtZero(true);
		stagesChart.setData(data);
	}
}
