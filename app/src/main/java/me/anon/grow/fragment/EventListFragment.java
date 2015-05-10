package me.anon.grow.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import me.anon.controller.adapter.PlantAdapter;
import me.anon.grow.AddPlantActivity;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class EventListFragment extends Fragment
{
	private PlantAdapter adapter;

	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;

	private int plantIndex = -1;
	private Plant plant;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static EventListFragment newInstance(int plantIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);

		EventListFragment fragment = new EventListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.event_list_view, container, false);
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
			getActivity().finish();
			return;
		}

		adapter = new PlantAdapter();
		adapter.setPlants(PlantManager.getInstance().getPlants());
		recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		recycler.setAdapter(adapter);
	}

	@Views.OnClick public void onFabAddClick(View view)
	{
		Intent addPlant = new Intent(getActivity(), AddPlantActivity.class);
		startActivity(addPlant);
	}
}
