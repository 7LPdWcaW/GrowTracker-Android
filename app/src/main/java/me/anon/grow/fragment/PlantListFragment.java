package me.anon.grow.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import me.anon.controller.adapter.PlantAdapter;
import me.anon.controller.adapter.SimpleItemTouchHelperCallback;
import me.anon.grow.AddPlantActivity;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
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

		getActivity().setTitle("Your plants");

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

		int plantsSize =  PlantManager.getInstance().getPlants().size();
		ArrayList<Plant> ordered = new ArrayList<>();
		ordered.addAll(Arrays.asList(new Plant[plantsSize]));

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		for (int index = 0; index < plantsSize; index++)
		{
			Plant plant = PlantManager.getInstance().getPlants().get(index);
			ordered.set(prefs.getInt(String.valueOf(index), ordered.size()), plant);
		}

		adapter.setPlants(ordered);
		adapter.notifyDataSetChanged();
	}

	@Override public void onPause()
	{
		super.onPause();

		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		int plantsSize = adapter.getItemCount();

		for (int index = 0; index < plantsSize; index++)
		{
			prefs.putInt(String.valueOf(PlantManager.getInstance().getPlants().indexOf(adapter.getPlants().get(index))), index);
		}

		prefs.apply();
	}

	@Views.OnClick public void onFabAddClick(View view)
	{
		Intent addPlant = new Intent(getActivity(), AddPlantActivity.class);
		startActivity(addPlant);
	}
}
