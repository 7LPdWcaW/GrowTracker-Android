package me.anon.grow.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.Plant;
import me.anon.model.PlantMedium;
import me.anon.model.Water;

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
	@Views.InjectView(R.id.temp_container) private View tempContainer;
	@Views.InjectView(R.id.temp) private TextView temp;
	@Views.InjectView(R.id.date_container) private View dateContainer;
	@Views.InjectView(R.id.date) private TextView date;
	@Views.InjectView(R.id.additive_container) private ViewGroup additiveContainer;
	@Views.InjectView(R.id.notes) private EditText notes;

	private int plantIndex = -1;
	private int actionIndex = -1;
	private Plant plant;
	private Water water;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static WateringFragment newInstance(int plantIndex, int feedingIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);
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

		if (getArguments() != null)
		{
			plantIndex = getArguments().getInt("plant_index");
			actionIndex = getArguments().getInt("action_index");

			if (plantIndex > -1)
			{
				plant = PlantManager.getInstance().getPlants().get(plantIndex);
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}

			if (actionIndex > -1)
			{
				water = (Water)PlantManager.getInstance().getPlants().get(plantIndex).getActions().get(actionIndex);
			}
		}

		if (water == null)
		{
			water = new Water();
		}

		if (plant == null)
		{
			getActivity().finish();
			return;
		}

		setUi();
		setHints();
	}

	private void setHints()
	{
		if (water != null)
		{
			Water hintFeed = null;

			for (int index = plant.getActions().size() - 1; index >= 0; index--)
			{
				if (plant.getActions().get(index).getClass() == Water.class)
				{
					hintFeed = (Water)plant.getActions().get(index);
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
					amount.setHint(String.valueOf(hintFeed.getAmount()));
				}

				if (plant.getMedium() == PlantMedium.HYDRO)
				{
					tempContainer.setVisibility(View.VISIBLE);

					if (hintFeed.getTemp() != null)
					{
						temp.setHint(String.valueOf(hintFeed.getTemp()));
					}
				}

				notes.setHint(hintFeed.getNotes());
			}
		}
	}

	private void setUi()
	{
		getActivity().setTitle("Feeding " + plant.getName());

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

		if (water.getPh() != null)
		{
			waterPh.setText(String.valueOf(water.getPh()));
		}

		if (water.getPpm() != null)
		{
			waterPpm.setText(String.valueOf(water.getPpm()));
		}

		if (water.getRunoff() != null)
		{
			runoffPh.setText(String.valueOf(water.getRunoff()));
		}

		if (water.getAmount() != null)
		{
			amount.setText(String.valueOf(water.getAmount()));
		}

		if (plant.getMedium() == PlantMedium.HYDRO)
		{
			tempContainer.setVisibility(View.VISIBLE);

			if (water.getTemp() != null)
			{
				temp.setText(String.valueOf(water.getTemp()));
			}
		}

		notes.setText(water.getNotes());
	}

	@Views.OnClick public void onNewAdditiveClick(View view)
	{
		final Object currentTag = view.getTag();
		FragmentManager fm = getFragmentManager();
		AddAdditiveDialogFragment addAdditiveDialogFragment = new AddAdditiveDialogFragment(view.getTag() instanceof Additive ? (Additive)view.getTag() : null);
		addAdditiveDialogFragment.setOnAdditiveSelectedListener(new AddAdditiveDialogFragment.OnAdditiveSelectedListener()
		{
			@Override public void onAdditiveSelected(Additive additive)
			{
				View additiveStub = LayoutInflater.from(getActivity()).inflate(R.layout.additive_stub, additiveContainer, false);
				((TextView)additiveStub).setText(additive.getDescription() + "   -   " + additive.getAmount() + "ml/l");

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
							((TextView)additiveContainer.getChildAt(childIndex)).setText(additive.getDescription() + "   -   " + additive.getAmount() + "ml/l");
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
		Long ppm = TextUtils.isEmpty(this.waterPpm.getText()) ? null : Long.valueOf(this.waterPpm.getText().toString());
		Double runoffPh = TextUtils.isEmpty(this.runoffPh.getText()) ? null : Double.valueOf(this.runoffPh.getText().toString());
		Integer amount = TextUtils.isEmpty(this.amount.getText()) ? null : Integer.valueOf(this.amount.getText().toString());
		Integer temp = TextUtils.isEmpty(this.temp.getText()) ? null : Integer.valueOf(this.temp.getText().toString());

		water.setPh(waterPh);
		water.setPpm(ppm);
		water.setRunoff(runoffPh);
		water.setAmount(amount);
		water.setTemp(temp);
		water.setNotes(TextUtils.isEmpty(notes.getText().toString()) ? null : notes.getText().toString());

		if (plant.getActions() == null)
		{
			plant.setActions(new ArrayList<Action>());
		}

		if (actionIndex < 0)
		{
			plant.getActions().add(water);
		}
		else
		{
			plant.getActions().set(actionIndex, water);
		}

		PlantManager.getInstance().upsert(plantIndex, plant);
		getActivity().setResult(Activity.RESULT_OK);
		getActivity().finish();
	}
}
