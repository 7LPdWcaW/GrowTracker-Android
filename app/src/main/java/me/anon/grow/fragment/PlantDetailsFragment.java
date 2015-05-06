package me.anon.grow.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Locale;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;
import me.anon.model.PlantStage;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class PlantDetailsFragment extends Fragment
{
	private final String[] stages = {"Germination", "Vegetation", "Flower", "Curing"};

	@Views.InjectView(R.id.action_container) private View actionContainer;
	@Views.InjectView(R.id.link_container) private View linkContainer;

	@Views.InjectView(R.id.plant_name) private TextView name;
	@Views.InjectView(R.id.plant_strain) private TextView strain;
	@Views.InjectView(R.id.plant_stage) private TextView stage;

	private int plantIndex = -1;
	private Plant plant;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static PlantDetailsFragment newInstance(int plantIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);

		PlantDetailsFragment fragment = new PlantDetailsFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.plant_details_view, container, false);
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

		if (plant == null)
		{
			plant = new Plant();
		}
		else
		{
			actionContainer.setVisibility(View.VISIBLE);
			linkContainer.setVisibility(View.VISIBLE);

			name.setText(plant.getName());
			strain.setText(plant.getStrain());

			if (plant.getStage() != null)
			{
				stage.setText(stages[plant.getStage().ordinal()]);
			}
		}
	}

	@Views.OnClick public void onFeedingClick(final View view)
	{

	}

	@Views.OnClick public void onPlantStageClick(final View view)
	{
		new AlertDialog.Builder(view.getContext())
			.setTitle("Strain")
			.setItems(stages, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					((TextView)view).setText(stages[which]);
				}
			})
			.show();
	}

	@Views.OnClick public void onFabCompleteClick(final View view)
	{
		name.setError(null);
		strain.setError(null);

		if (!TextUtils.isEmpty(name.getText()))
		{
			plant.setName(name.getText().toString().trim());
		}
		else
		{
			name.setError("Name can not be empty");
			return;
		}

		if (!TextUtils.isEmpty(strain.getText()))
		{
			plant.setStrain(strain.getText().toString().trim());
		}
		else
		{
			name.setError("Name can not be empty");
			return;
		}

		plant.setStage(PlantStage.valueOf(stage.getText().toString().toUpperCase(Locale.ENGLISH)));

		PlantManager.getInstance().upsert(plantIndex, plant);
		getActivity().finish();
	}
}
