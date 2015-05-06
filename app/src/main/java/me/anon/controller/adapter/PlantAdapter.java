package me.anon.controller.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.model.Plant;
import me.anon.view.PlantHolder;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class PlantAdapter extends RecyclerView.Adapter<PlantHolder>
{
	@Getter @Setter private List<Plant> plants = new ArrayList<>();

	@Override public PlantHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{
		return new PlantHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plant_item, viewGroup, false));
	}

	@Override public void onBindViewHolder(PlantHolder viewHolder, final int i)
	{
		final Plant plant = plants.get(i);
		viewHolder.getName().setText(plant.getName());
		viewHolder.getSummary().setText(plant.getStrain() + " - " + plant.getStage());

		viewHolder.itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				Intent details = new Intent(v.getContext(), PlantDetailsActivity.class);
				details.putExtra("plant_index", i);
				v.getContext().startActivity(details);
			}
		});
	}

	@Override public int getItemCount()
	{
		return plants.size();
	}
}
