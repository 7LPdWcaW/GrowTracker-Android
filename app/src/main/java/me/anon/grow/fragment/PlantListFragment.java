package me.anon.grow.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.Setter;
import me.anon.controller.adapter.PlantAdapter;
import me.anon.controller.adapter.SimpleItemTouchHelperCallback;
import me.anon.grow.AddPlantActivity;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Garden;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class PlantListFragment extends Fragment
{
	private PlantAdapter adapter;
	@Setter private Garden garden;

	public static PlantListFragment newInstance(@Nullable Garden garden)
	{
		PlantListFragment fragment = new PlantListFragment();
		fragment.setGarden(garden);

		return fragment;
	}

	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.plant_list_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle(garden == null ? "All" : garden.getName() + " plants");

		adapter = new PlantAdapter(getActivity());
		recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		recycler.setAdapter(adapter);

		ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		touchHelper.attachToRecyclerView(recycler);
	}

	@Override public void onResume()
	{
		super.onResume();

		adapter.setPlants(PlantManager.getInstance().getSortedPlantList());
		adapter.notifyDataSetChanged();
	}

	@Override public void onDestroy()
	{
		super.onDestroy();

		ArrayList<Plant> plants = new ArrayList<Plant>();
		plants.addAll(new ArrayList(Arrays.asList(new Plant[adapter.getItemCount()])));

		for (Plant plant : PlantManager.getInstance().getPlants())
		{
			int adapterIndex = adapter.getPlants().indexOf(plant);

			if (adapterIndex > -1)
			{
				plants.set(adapterIndex, plant);
			}
			else
			{
				plants.add(plant);
			}
		}

		PlantManager.getInstance().setPlants(plants);
		PlantManager.getInstance().save();
	}

	@Views.OnClick public void onFabAddClick(View view)
	{
		Intent addPlant = new Intent(getActivity(), AddPlantActivity.class);
		startActivity(addPlant);
	}
}
