package me.anon.grow.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.model.Plant;

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
	@Views.InjectView(R.id.plant_name) private TextView name;
	@Views.InjectView(R.id.plant_strain) private TextView strain;
	@Views.InjectView(R.id.plant_stage) private TextView stage;

	/**
	 * @param plant If null, asume new plant
	 * @return Instantiated details fragment
	 */
	public static PlantDetailsFragment newInstance(@Nullable Plant plant)
	{
		Bundle args = new Bundle();

		if (plant != null)
		{
			args.putString("plant", new Gson().toJson(plant));
		}

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
	}

	@Views.OnClick public void onPlantStageClick(final View view)
	{
		final String[] stages = {"Germination", "Vegetation", "Flower", "Curing"};

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

	}
}
