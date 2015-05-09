package me.anon.grow.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Feed;
import me.anon.model.Nutrient;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class AddFeedingFragment extends Fragment
{
	@Views.InjectView(R.id.water_ph) private TextView waterPh;
	@Views.InjectView(R.id.water_ppm) private TextView waterPpm;
	@Views.InjectView(R.id.runoff_ph) private TextView runoffPh;
	@Views.InjectView(R.id.amount) private TextView amount;
	@Views.InjectView(R.id.nutrient) private TextView nutrient;
	@Views.InjectView(R.id.nutrient_amount) private TextView nutrientAmount;

	private int plantIndex = -1;
	private Plant plant;
	private Feed feed;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static AddFeedingFragment newInstance(int plantIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);

		AddFeedingFragment fragment = new AddFeedingFragment();
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

			if (plantIndex > -1)
			{
				plant = PlantManager.getInstance().getPlants().get(plantIndex);
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		}

		feed = new Feed();

		if (plant == null)
		{
			getActivity().finish();
			return;
		}

		nutrient.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
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
								if (action instanceof Feed)
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
							AddFeedingFragment.this.nutrient.setText(nutrient.getNpc() + ":" + nutrient.getPpc() + ":" + nutrient.getKpc());
						}
					});
					addNutrientDialogFragment.show(fm, "fragment_add_nutrient");
				}
			}
		});
	}

	@Views.OnClick public void onFabCompleteClick(final View view)
	{
		double waterPh = Double.valueOf(TextUtils.isEmpty(this.waterPh.getText()) ? "0.0" : this.waterPh.getText().toString());
		int ppm = Integer.valueOf(TextUtils.isEmpty(this.waterPpm.getText()) ? "0" : this.waterPh.getText().toString());
		double runoffPh = Double.valueOf(TextUtils.isEmpty(this.runoffPh.getText()) ? "0.0" : this.runoffPh.getText().toString());
		int amount = Integer.valueOf(TextUtils.isEmpty(this.amount.getText()) ? "0" : this.amount.getText().toString());
		int nutrientAmount = Integer.valueOf(TextUtils.isEmpty(this.nutrientAmount.getText()) ? "0" : this.nutrientAmount.getText().toString());

		feed.setPh(waterPh);
		feed.setPpm(ppm);
		feed.setRunoff(runoffPh);
		feed.setAmount(amount);
		feed.setMlpl(nutrientAmount);

		if (plant.getActions() == null)
		{
			plant.setActions(new ArrayList<Action>());
		}

		plant.getActions().add(feed);
		PlantManager.getInstance().upsert(plantIndex, plant);
		getActivity().finish();
	}
}
