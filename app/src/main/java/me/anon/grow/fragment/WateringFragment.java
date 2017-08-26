package me.anon.grow.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.grow.R;
import me.anon.lib.TempUnit;
import me.anon.lib.Unit;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.Plant;
import me.anon.model.PlantMedium;
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

	private void setHints()
	{
		amountLabel.setText("Amount (" + selectedDeliveryUnit.getLabel() + ")");
		amount.setHint("250" + selectedDeliveryUnit.getLabel());

		if (usingEc)
		{
			waterPpm.setHint("1.0 EC");
			((TextView)((ViewGroup)waterPpm.getParent()).findViewById(R.id.ppm_label)).setText("EC");
		}

		if (water != null && plants.size() == 1)
		{
			Water hintFeed = null;

			for (int index = plants.get(0).getActions().size() - 1; index >= 0; index--)
			{
				if (plants.get(0).getActions().get(index).getClass() == Water.class)
				{
					hintFeed = (Water)plants.get(0).getActions().get(index);
					break;
				}
			}

			if (hintFeed != null)
			{
				if (hintFeed.getPh() != null)
				{
					waterPh.setHint(String.valueOf(hintFeed.getPh()));
				}

				if (hintFeed.getPpm() != null)
				{
					waterPpm.setHint(String.valueOf(hintFeed.getPpm()));
				}

				if (hintFeed.getRunoff() != null)
				{
					runoffPh.setHint(String.valueOf(hintFeed.getRunoff()));
				}

				if (hintFeed.getAmount() != null)
				{
					amount.setHint(String.valueOf(ML.to(selectedDeliveryUnit, hintFeed.getAmount())) + selectedDeliveryUnit.getLabel());
				}

				if (plants.get(0).getMedium() == PlantMedium.HYDRO || plants.get(0).getMedium() == PlantMedium.AERO)
				{
					tempContainer.setVisibility(View.VISIBLE);
					tempLabel.setText("Temp (º" + selectedTemperatureUnit.getLabel() + ")");

					if (hintFeed.getTemp() != null)
					{
						temp.setHint(String.valueOf(CELCIUS.to(selectedTemperatureUnit, hintFeed.getTemp())) + selectedTemperatureUnit.getLabel());
					}
				}

				notes.setHint(hintFeed.getNotes());
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

			if (plants.get(0).getMedium() == PlantMedium.HYDRO)
			{
				tempContainer.setVisibility(View.VISIBLE);

				if (water.getTemp() != null)
				{
					temp.setHint(String.valueOf(CELCIUS.to(selectedTemperatureUnit, water.getTemp())) + selectedTemperatureUnit.getLabel());
				}
			}

			for (Additive additive : water.getAdditives())
			{
				if (additive == null || additive.getAmount() == null) continue;

				double converted = Unit.ML.to(selectedMeasurementUnit, additive.getAmount());
				String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

				View additiveStub = LayoutInflater.from(getActivity()).inflate(R.layout.additive_stub, additiveContainer, false);
				((TextView)additiveStub).setText(additive.getDescription() + "   -   " + amountStr + selectedMeasurementUnit.getLabel() + "/" + selectedDeliveryUnit.getLabel());

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

			notes.setText(water.getNotes());
		}
	}

	@Views.OnClick public void onNewAdditiveClick(View view)
	{
		if (getActivity().getCurrentFocus() != null)
		{
			getActivity().getCurrentFocus().clearFocus();
		}

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

				double converted = Unit.ML.to(selectedMeasurementUnit, additive.getAmount());
				String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

				View additiveStub = LayoutInflater.from(getActivity()).inflate(R.layout.additive_stub, additiveContainer, false);
				((TextView)additiveStub).setText(additive.getDescription() + "   -   " + amountStr + selectedMeasurementUnit.getLabel() + "/" + selectedDeliveryUnit.getLabel());

				if (currentTag == null)
				{
					if (!water.getAdditives().contains(additive))
					{
						water.getAdditives().add(additive);

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
				}
				else
				{
					for (int childIndex = 0; childIndex < additiveContainer.getChildCount(); childIndex++)
					{
						Object tag = additiveContainer.getChildAt(childIndex).getTag();

						if (tag == currentTag)
						{
							converted = Unit.ML.to(selectedMeasurementUnit, additive.getAmount());
							amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

							water.getAdditives().set(childIndex, additive);

							((TextView)additiveContainer.getChildAt(childIndex)).setText(additive.getDescription() + "   -   " + amountStr + selectedMeasurementUnit.getLabel() + "/" + selectedDeliveryUnit.getLabel());
							additiveContainer.getChildAt(childIndex).setTag(additive);

							break;
						}
					}
				}

				additiveStub.requestFocus();
				additiveStub.requestFocusFromTouch();
			}

			@Override public void onAdditiveDeleteRequested(Additive additive)
			{
				if (water.getAdditives().contains(additive))
				{
					water.getAdditives().remove(additive);
				}

				for (int childIndex = 0; childIndex < additiveContainer.getChildCount(); childIndex++)
				{
					Object tag = additiveContainer.getChildAt(childIndex).getTag();

					if (tag == additive)
					{
						additiveContainer.removeViewAt(childIndex);
						break;
					}
				}

				additiveContainer.getChildAt(additiveContainer.getChildCount() - 1).requestFocus();
				additiveContainer.getChildAt(additiveContainer.getChildCount() - 1).requestFocusFromTouch();
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
}
