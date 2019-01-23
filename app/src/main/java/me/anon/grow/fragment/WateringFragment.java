package me.anon.grow.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.esotericsoftware.kryo.Kryo;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.grow.R;
import me.anon.lib.TempUnit;
import me.anon.lib.Unit;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.manager.ScheduleManager;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.FeedingSchedule;
import me.anon.model.FeedingScheduleDate;
import me.anon.model.Plant;
import me.anon.model.Water;

import static me.anon.lib.TempUnit.CELCIUS;
import static me.anon.lib.Unit.ML;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class WateringFragment extends Fragment
{
	@Views.InjectView(R.id.water_ph) private TextView waterPh;
	@Views.InjectView(R.id.water_ppm) private TextView waterPpm;
	@Views.InjectView(R.id.runoff_ph) private TextView runoffPh;
	@Views.InjectView(R.id.amount) private TextView amount;
	@Views.InjectView(R.id.amount_label) private TextView amountLabel;
	@Views.InjectView(R.id.temp_container) private View tempContainer;
	@Views.InjectView(R.id.temp) private TextView temp;
	@Views.InjectView(R.id.temp_label) private TextView tempLabel;
	@Views.InjectView(R.id.date_container) private View dateContainer;
	@Views.InjectView(R.id.date) private TextView date;
	@Views.InjectView(R.id.additive_container) private ViewGroup additiveContainer;
	@Views.InjectView(R.id.notes) private EditText notes;

	private int[] plantIndex = {-1};
	private int actionIndex = -1;
	private ArrayList<Plant> plants = new ArrayList<>();
	private Water water;
	private Unit selectedMeasurementUnit, selectedDeliveryUnit;
	private TempUnit selectedTemperatureUnit;
	private boolean usingEc = false;
	private TextWatcher deliveryTextChangeListener = new TextWatcher()
	{
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		@Override public void onTextChanged(CharSequence s, int start, int before, int count){}

		@Override public void afterTextChanged(Editable s)
		{
			populateAdditives();
		}
	};

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static WateringFragment newInstance(int[] plantIndex, int feedingIndex)
	{
		Bundle args = new Bundle();
		args.putIntArray("plant_index", plantIndex);
		args.putInt("action_index", feedingIndex);

		WateringFragment fragment = new WateringFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.add_watering_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		selectedMeasurementUnit = Unit.getSelectedMeasurementUnit(getActivity());
		selectedDeliveryUnit = Unit.getSelectedDeliveryUnit(getActivity());
		selectedTemperatureUnit = TempUnit.getSelectedTemperatureUnit(getActivity());
		usingEc = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("tds_ec", false);

		if (getArguments() != null)
		{
			plantIndex = getArguments().getIntArray("plant_index");
			actionIndex = getArguments().getInt("action_index");

			for (int index : plantIndex)
			{
				Plant plant = PlantManager.getInstance().getPlants().get(index);
				plants.add(plant);

				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}

			if (actionIndex > -1 && plantIndex.length == 1)
			{
				water = (Water)PlantManager.getInstance().getPlants().get(plantIndex[0]).getActions().get(actionIndex);
			}
		}

		if (water == null)
		{
			water = new Water();
		}

		if (plants.size() < 1)
		{
			getActivity().finish();
			return;
		}

		setUi();
		setHints();
	}

	@Override public void onResume()
	{
		super.onResume();

		if (getActivity() != null && getActivity().getCurrentFocus() != null)
		{
			InputMethodManager manager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

			getActivity().getCurrentFocus().clearFocus();
		}
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.feeding_menu, menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_populate_feeding)
		{
			ArrayList<String> items = new ArrayList<>();
			for (FeedingSchedule feedingSchedule : ScheduleManager.instance.getSchedules())
			{
				items.add(feedingSchedule.getName());
			}

			new AlertDialog.Builder(getActivity())
				.setTitle("Select schedule")
				.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						showScheduleDialog(ScheduleManager.instance.getSchedules().get(which));
					}
				})
				.show();
		}

		return super.onOptionsItemSelected(item);
	}

	private void showScheduleDialog(FeedingSchedule schedule)
	{
		FeedingSelectDialogFragment feedingSelectDialogFragment = new FeedingSelectDialogFragment(schedule, plants.get(0));
		feedingSelectDialogFragment.setOnFeedingSelectedListener(new FeedingSelectDialogFragment.OnFeedingSelectedListener()
		{
			@Override public void onFeedingSelected(FeedingScheduleDate date)
			{
				water.setAdditives(date.getAdditives());
				populateAdditives();
			}
		});
		feedingSelectDialogFragment.show(getFragmentManager(), "feeding");
	}

	private void setHints()
	{
		amountLabel.setText("Amount (" + selectedDeliveryUnit.getLabel() + ")");
		amount.setHint("250" + selectedDeliveryUnit.getLabel());

		if (usingEc)
		{
			waterPpm.setHint("1.0 EC");
			((TextView)((ViewGroup)waterPpm.getParent()).findViewById(R.id.ppm_label)).setText("EC");
		}

		if (water != null)
		{
			ArrayList<Water> hintFeed = new ArrayList<>();

			for (Plant plant : plants)
			{
				for (int index = plant.getActions().size() - 1; index >= 0; index--)
				{
					if (plant.getActions().get(index).getClass() == Water.class)
					{
						hintFeed.add((Water)plant.getActions().get(index));
						break;
					}
				}
			}

			if (hintFeed.size() > 0)
			{
				Double averagePh = 0.0;
				int phCount = 0;
				Double averagePpm = 0.0;
				int ppmCount = 0;
				Double averageRunoff = 0.0;
				int runoffCount = 0;
				Double averageAmount = 0.0;
				int amountCount = 0;
				Double averageTemp = 0.0;
				int tempCount = 0;

				for (Water hint : hintFeed)
				{
					if (hint.getPh() != null)
					{
						averagePh += hint.getPh();
						phCount++;
					}

					if (hint.getPpm() != null)
					{
						averagePpm += hint.getPpm();
						ppmCount++;
					}

					if (hint.getRunoff() != null)
					{
						averageRunoff += hint.getRunoff();
						runoffCount++;
					}

					if (hint.getAmount() != null)
					{
						averageAmount += hint.getAmount();
						amountCount++;
					}

					if (hint.getTemp() != null)
					{
						averageTemp += hint.getTemp();
						tempCount++;
					}
				}

				averagePh = averagePh / phCount;
				averagePpm = averagePpm / ppmCount;
				averageRunoff = averageRunoff / runoffCount;
				averageAmount = averageAmount / amountCount;
				averageTemp = averageTemp / tempCount;

				if (!averagePh.isNaN())
				{
					waterPh.setHint(String.valueOf(averagePh));
				}

				if (!averagePpm.isNaN())
				{
					waterPpm.setHint(String.valueOf(averagePpm));
				}

				if (!averageRunoff.isNaN())
				{
					runoffPh.setHint(String.valueOf(averageRunoff));
				}

				if (!averageAmount.isNaN())
				{
					amount.setHint(String.valueOf(ML.to(selectedDeliveryUnit, averageAmount)) + selectedDeliveryUnit.getLabel());
				}

				tempContainer.setVisibility(View.VISIBLE);
				tempLabel.setText("Temp (º" + selectedTemperatureUnit.getLabel() + ")");

				if (!averageTemp.isNaN())
				{
					temp.setHint(String.valueOf(CELCIUS.to(selectedTemperatureUnit, averageTemp)) + selectedTemperatureUnit.getLabel());
				}

				notes.setHint(hintFeed.get(0).getNotes());
			}
		}
	}

	private void setUi()
	{
		getActivity().setTitle("Feeding " + (plants.size() == 1 ? plants.get(0).getName() : "multiple plants"));

		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(water.getDate());

		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		String dateStr = dateFormat.format(new Date(water.getDate())) + " " + timeFormat.format(new Date(water.getDate()));
		this.date.setText(dateStr);

		this.dateContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				final DateDialogFragment fragment = new DateDialogFragment(water.getDate());
				fragment.setOnDateSelected(new DateDialogFragment.OnDateSelectedListener()
				{
					@Override public void onDateSelected(Calendar date)
					{
						String dateStr = dateFormat.format(date.getTime()) + " " + timeFormat.format(date.getTime());
						WateringFragment.this.date.setText(dateStr);

						water.setDate(date.getTimeInMillis());
						onCancelled();
					}

					@Override public void onCancelled()
					{
						getFragmentManager().beginTransaction().remove(fragment).commit();
					}
				});
				getFragmentManager().beginTransaction().add(fragment, "date").commit();
			}
		});

		if (plants.size() == 1)
		{
			if (water.getPh() != null)
			{
				waterPh.setText(String.valueOf(water.getPh()));
			}

			if (water.getPpm() != null)
			{
				String ppm = String.valueOf(water.getPpm().longValue());
				if (usingEc)
				{
					ppm = String.valueOf((water.getPpm() * 2d) / 1000d);
				}

				waterPpm.setText(ppm);
			}

			if (water.getRunoff() != null)
			{
				runoffPh.setText(String.valueOf(water.getRunoff()));
			}

			if (water.getAmount() != null)
			{
				amount.setText(String.valueOf(ML.to(selectedDeliveryUnit, water.getAmount())));
			}

			tempContainer.setVisibility(View.VISIBLE);

			if (water.getTemp() != null)
			{
				temp.setHint(String.valueOf(CELCIUS.to(selectedTemperatureUnit, water.getTemp())) + selectedTemperatureUnit.getLabel());
			}

			populateAdditives();
			notes.setText(water.getNotes());
		}
	}

	private void populateAdditives()
	{
		additiveContainer.removeViews(0, additiveContainer.getChildCount() - 1);
		int maxChars = 0;

		for (Additive additive : water.getAdditives())
		{
			if (additive == null || additive.getAmount() == null) continue;

			double converted = Unit.ML.to(selectedMeasurementUnit, additive.getAmount());
			String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);
			amountStr = additive.getDescription() + "   -   " + amountStr + selectedMeasurementUnit.getLabel() + "/" + selectedDeliveryUnit.getLabel();
			maxChars = Math.max(maxChars, amountStr.length());
		}

		for (Additive additive : water.getAdditives())
		{
			if (additive == null || additive.getAmount() == null) continue;

			double converted = Unit.ML.to(selectedMeasurementUnit, additive.getAmount());
			String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

			View additiveStub = LayoutInflater.from(getActivity()).inflate(R.layout.additive_stub, additiveContainer, false);
			amountStr = additive.getDescription() + "&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;" + amountStr + selectedMeasurementUnit.getLabel() + "/" + selectedDeliveryUnit.getLabel();

			Double totalDelivery = water.getAmount();
			if (totalDelivery == null || !TextUtils.isEmpty(amount.getText().toString()))
			{
				if (totalDelivery == null || totalDelivery != Double.parseDouble(amount.getText().toString()))
				{
					try
					{
						totalDelivery = selectedDeliveryUnit.to(Unit.ML, Double.parseDouble(amount.getText().toString()));
					}
					catch (NumberFormatException e)
					{
						totalDelivery = null;
					}
				}
			}

			if (totalDelivery != null)
			{
				totalDelivery = ML.to(selectedDeliveryUnit, totalDelivery);
				Double additiveAmount = ML.to(selectedMeasurementUnit, additive.getAmount());

				amountStr = amountStr + "&nbsp;&nbsp;<b>(" + Unit.toTwoDecimalPlaces(additiveAmount * totalDelivery) + selectedMeasurementUnit.getLabel() + " total)</b>";
			}

			((TextView)additiveStub).setText(Html.fromHtml(amountStr));

			additiveStub.setTag(additive);
			additiveStub.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					onNewAdditiveClick(view);
				}
			});
			additiveContainer.addView(additiveStub, additiveContainer.getChildCount() - 1);
		}

		amount.removeTextChangedListener(deliveryTextChangeListener);
		amount.addTextChangedListener(deliveryTextChangeListener);
	}

	@Views.OnClick public void onNewAdditiveClick(View view)
	{
		if (getActivity().getCurrentFocus() != null)
		{
			getActivity().getCurrentFocus().clearFocus();
		}

		final View focus = getActivity().getCurrentFocus();
		final Object currentTag = view.getTag();
		FragmentManager fm = getFragmentManager();
		AddAdditiveDialogFragment addAdditiveDialogFragment = new AddAdditiveDialogFragment(view.getTag() instanceof Additive ? (Additive)view.getTag() : null);
		addAdditiveDialogFragment.setOnAdditiveSelectedListener(new AddAdditiveDialogFragment.OnAdditiveSelectedListener()
		{
			@Override public void onAdditiveSelected(Additive additive)
			{
				if (TextUtils.isEmpty(additive.getDescription()))
				{
					return;
				}

				if (!water.getAdditives().contains(additive))
				{
					water.getAdditives().add(additive);
				}
				else
				{
					for (int childIndex = 0; childIndex < additiveContainer.getChildCount(); childIndex++)
					{
						Object tag = additiveContainer.getChildAt(childIndex).getTag();

						if (tag == currentTag)
						{
							water.getAdditives().set(childIndex, additive);
							break;
						}
					}
				}

				populateAdditives();

				if (focus != null)
				{
					focus.requestFocus();
					focus.requestFocusFromTouch();
				}
			}

			@Override public void onAdditiveDeleteRequested(Additive additive)
			{
				water.getAdditives().remove(additive);

				for (int childIndex = 0; childIndex < additiveContainer.getChildCount(); childIndex++)
				{
					Object tag = additiveContainer.getChildAt(childIndex).getTag();

					if (tag == additive)
					{
						additiveContainer.removeViewAt(childIndex);
						break;
					}
				}

				if (focus != null)
				{
					focus.requestFocus();
					focus.requestFocusFromTouch();
				}
			}
		});

		addAdditiveDialogFragment.show(fm, "fragment_add_additive");
	}

	@Views.OnClick public void onFabCompleteClick(final View view)
	{
		Double waterPh = TextUtils.isEmpty(this.waterPh.getText()) ? null : Double.valueOf(this.waterPh.getText().toString());
		Double ppm = TextUtils.isEmpty(this.waterPpm.getText()) ? null : Double.valueOf(this.waterPpm.getText().toString());
		Double runoffPh = TextUtils.isEmpty(this.runoffPh.getText()) ? null : Double.valueOf(this.runoffPh.getText().toString());
		Double amount = TextUtils.isEmpty(this.amount.getText()) ? null : Double.valueOf(this.amount.getText().toString());
		Double temp = TextUtils.isEmpty(this.temp.getText()) ? null : Double.valueOf(this.temp.getText().toString());

		if (usingEc && ppm != null)
		{
			ppm = (ppm * 1000d) / 2d;
		}

		water.setPh(waterPh);
		water.setPpm(ppm);
		water.setRunoff(runoffPh);
		water.setAmount(amount == null ? null : selectedDeliveryUnit.to(ML, amount));

		if (temp != null)
		{
			water.setTemp(selectedTemperatureUnit.to(CELCIUS, temp));
		}

		water.setNotes(TextUtils.isEmpty(notes.getText().toString()) ? null : notes.getText().toString());

		if (plants.size() == 1)
		{
			if (plants.get(0).getActions() == null)
			{
				plants.get(0).setActions(new ArrayList<Action>());
			}

			if (actionIndex < 0)
			{
				plants.get(0).getActions().add(water);
			}
			else
			{
				plants.get(0).getActions().set(actionIndex, water);
			}

			PlantManager.getInstance().upsert(plantIndex[0], plants.get(0));
		}
		else
		{
			int index = 0;
			for (Plant plant : plants)
			{
				if (plant.getActions() == null)
				{
					plant.setActions(new ArrayList<Action>());
				}

				plant.getActions().add(new Kryo().copy(water));
			}

			PlantManager.getInstance().save();
		}

		PlantWidgetProvider.triggerUpdateAll(getActivity());
		getActivity().setResult(Activity.RESULT_OK);
		getActivity().finish();
	}

	@Views.OnClick(R.id.date_now) public void onDateNowClick(View view)
	{
		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		Calendar date = GregorianCalendar.getInstance();
		String dateStr = dateFormat.format(date.getTime()) + " " + timeFormat.format(date.getTime());
		WateringFragment.this.date.setText(dateStr);

		water.setDate(date.getTimeInMillis());
	}
}
