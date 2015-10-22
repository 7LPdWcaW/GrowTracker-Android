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
import me.anon.model.Feed;
import me.anon.model.Nutrient;
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
public class FeedingFragment extends Fragment
{
	@Views.InjectView(R.id.water_ph) private TextView waterPh;
	@Views.InjectView(R.id.water_ppm) private TextView waterPpm;
	@Views.InjectView(R.id.runoff_ph) private TextView runoffPh;
	@Views.InjectView(R.id.amount) private TextView amount;
	@Views.InjectView(R.id.temp_container) private View tempContainer;
	@Views.InjectView(R.id.temp) private TextView temp;
	@Views.InjectView(R.id.date_container) private View dateContainer;
	@Views.InjectView(R.id.date) private TextView date;
	@Views.InjectView(R.id.nutrient_container) private View nutrientContainer;
	@Views.InjectView(R.id.nutrient_nutrient_container) private View nutrientNutrientContainer;
	@Views.InjectView(R.id.nutrient) private TextView nutrient;
	@Views.InjectView(R.id.nutrient_amount) private TextView nutrientAmount;
	@Views.InjectView(R.id.notes) private EditText notes;

	private int plantIndex = -1;
	private int actionIndex = -1;
	private Plant plant;
	private Feed feed;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static FeedingFragment newInstance(int plantIndex, int feedingIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);
		args.putInt("action_index", feedingIndex);

		FeedingFragment fragment = new FeedingFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.add_feeding_view, container, false);
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
				if (PlantManager.getInstance().getPlants().get(plantIndex).getActions().get(actionIndex) instanceof Feed)
				{
					feed = (Feed)PlantManager.getInstance().getPlants().get(plantIndex).getActions().get(actionIndex);
				}
				else if (PlantManager.getInstance().getPlants().get(plantIndex).getActions().get(actionIndex) instanceof Water)
				{
					Water water = (Water)PlantManager.getInstance().getPlants().get(plantIndex).getActions().get(actionIndex);

					feed = new Feed();
					feed.setDate(water.getDate());
					feed.setPh(water.getPh());
					feed.setPpm(water.getPpm());
					feed.setRunoff(water.getRunoff());
					feed.setAmount(water.getAmount());
					feed.setTemp(water.getTemp());
					feed.setNotes(water.getNotes());
				}
			}
		}

		if (feed == null)
		{
			feed = new Feed();
		}

		if (plant == null)
		{
			getActivity().finish();
			return;
		}

		setUi();
		nutrientNutrientContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				Nutrient nutrient = feed.getNutrient();
				if (feed.getNutrient() == null)
				{
					if (plant.getActions() != null)
					{
						ArrayList<Action> actions = plant.getActions();
						for (int i = actions.size() - 1; i >= 0; i--)
						{
							Action action = actions.get(i);
							if (action instanceof Feed && ((Feed)action).getNutrient() != null)
							{
								nutrient = ((Feed)action).getNutrient();
								break;
							}
						}
					}
				}

				FragmentManager fm = getFragmentManager();
				AddNutrientDialogFragment addNutrientDialogFragment = new AddNutrientDialogFragment(nutrient);
				addNutrientDialogFragment.setOnAddNutrientListener(new AddNutrientDialogFragment.OnAddNutrientListener()
				{
					@Override public void onNutrientSelected(Nutrient nutrient)
					{
						feed.setNutrient(nutrient);

						if (nutrient == null)
						{
							feed.setAmount(null);
							FeedingFragment.this.nutrientAmount.setText(null);
							FeedingFragment.this.nutrient.setText("N/A");
						}
						else
						{
							String nutrientStr = "";
							nutrientStr += nutrient.getNpc() == null ? "-" : nutrient.getNpc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getPpc() == null ? "-" : nutrient.getPpc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getKpc() == null ? "-" : nutrient.getKpc();
							nutrientStr += "/";
							nutrientStr += nutrient.getCapc() == null ? "-" : nutrient.getCapc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getSpc() == null ? "-" : nutrient.getSpc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getMgpc() == null ? "-" : nutrient.getMgpc();

							FeedingFragment.this.nutrient.setText(nutrientStr);
						}
					}
				});
				addNutrientDialogFragment.show(fm, "fragment_add_nutrient");
			}
		});
		nutrient.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					nutrientNutrientContainer.performClick();
					nutrientNutrientContainer.requestFocusFromTouch();
				}
			}
		});
	}

	private void setUi()
	{
		getActivity().setTitle("Feeding " + plant.getName());

		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(feed.getDate());

		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		String dateStr = dateFormat.format(new Date(feed.getDate())) + " " + timeFormat.format(new Date(feed.getDate()));
		this.date.setText(dateStr);

		this.dateContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				final DateDialogFragment fragment = new DateDialogFragment(feed.getDate());
				fragment.setOnDateSelected(new DateDialogFragment.OnDateSelectedListener()
				{
					@Override public void onDateSelected(Calendar date)
					{
						String dateStr = dateFormat.format(date.getTime()) + " " + timeFormat.format(date.getTime());
						FeedingFragment.this.date.setText(dateStr);

						feed.setDate(date.getTimeInMillis());
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

		if (feed.getPh() != null)
		{
			waterPh.setText(String.valueOf(feed.getPh()));
		}

		if (feed.getPpm() != null)
		{
			waterPpm.setText(String.valueOf(feed.getPpm()));
		}

		if (feed.getRunoff() != null)
		{
			runoffPh.setText(String.valueOf(feed.getRunoff()));
		}

		if (feed.getAmount() != null)
		{
			amount.setText(String.valueOf(feed.getAmount()));
		}

		if (plant.getMedium() == PlantMedium.HYDRO)
		{
			tempContainer.setVisibility(View.VISIBLE);

			if (feed.getTemp() != null)
			{
				temp.setText(String.valueOf(feed.getTemp()));
			}
		}

		if (feed.getNutrient() != null)
		{
			String nutrientStr = "";
			nutrientStr += feed.getNutrient().getNpc() == null ? "-" : feed.getNutrient().getNpc();
			nutrientStr += " : ";
			nutrientStr += feed.getNutrient().getPpc() == null ? "-" : feed.getNutrient().getPpc();
			nutrientStr += " : ";
			nutrientStr += feed.getNutrient().getKpc() == null ? "-" : feed.getNutrient().getKpc();
			nutrientStr += "/";
			nutrientStr += feed.getNutrient().getCapc() == null ? "-" : feed.getNutrient().getCapc();
			nutrientStr += " : ";
			nutrientStr += feed.getNutrient().getSpc() == null ? "-" : feed.getNutrient().getSpc();
			nutrientStr += " : ";
			nutrientStr += feed.getNutrient().getMgpc() == null ? "-" : feed.getNutrient().getMgpc();

			nutrient.setText(nutrientStr);
		}

		if (feed.getMlpl() != null)
		{
			nutrientAmount.setText(String.valueOf(feed.getMlpl()));
		}

		notes.setText(feed.getNotes());
	}

	@Views.OnClick public void onFabCompleteClick(final View view)
	{
		Double waterPh = TextUtils.isEmpty(this.waterPh.getText()) ? null : Double.valueOf(this.waterPh.getText().toString());
		Long ppm = TextUtils.isEmpty(this.waterPpm.getText()) ? null : Long.valueOf(this.waterPpm.getText().toString());
		Double runoffPh = TextUtils.isEmpty(this.runoffPh.getText()) ? null : Double.valueOf(this.runoffPh.getText().toString());
		Integer amount = TextUtils.isEmpty(this.amount.getText()) ? null : Integer.valueOf(this.amount.getText().toString());
		Integer temp = TextUtils.isEmpty(this.temp.getText()) ? null : Integer.valueOf(this.temp.getText().toString());
		Double nutrientAmount = TextUtils.isEmpty(this.nutrientAmount.getText()) ? null : Double.valueOf(this.nutrientAmount.getText().toString());

		feed.setPh(waterPh);
		feed.setPpm(ppm);
		feed.setRunoff(runoffPh);
		feed.setAmount(amount);
		feed.setTemp(temp);
		feed.setMlpl(nutrientAmount);
		feed.setNotes(TextUtils.isEmpty(notes.getText().toString()) ? null : notes.getText().toString());

		if (plant.getActions() == null)
		{
			plant.setActions(new ArrayList<Action>());
		}

		if (feed.getNutrient() == null)
		{
			Water water = new Water();
			water.setPh(feed.getPh());
			water.setPpm(feed.getPpm());
			water.setRunoff(feed.getRunoff());
			water.setAmount(feed.getAmount());
			water.setTemp(feed.getTemp());
			water.setDate(feed.getDate());
			water.setNotes(feed.getNotes());

			if (actionIndex < 0)
			{
				plant.getActions().add(water);
			}
			else
			{
				plant.getActions().set(actionIndex, water);
			}
		}
		else
		{
			if (actionIndex < 0)
			{
				plant.getActions().add(feed);
			}
			else
			{
				plant.getActions().set(actionIndex, feed);
			}
		}

		PlantManager.getInstance().upsert(plantIndex, plant);
		getActivity().setResult(Activity.RESULT_OK);
		getActivity().finish();
	}
}
