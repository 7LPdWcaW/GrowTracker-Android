package me.anon.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import me.anon.grow.R;
import me.anon.lib.Unit;
import me.anon.model.Plant;
import me.anon.view.PlantHolder;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class PlantAdapter extends RecyclerView.Adapter implements ItemTouchHelperAdapter
{
	private List<Plant> plants = new ArrayList<>();
	private List<String> showOnly = null;
	private Unit measureUnit, deliveryUnit;
	private int cardStyle = 2;

	public void setShowOnly(List<String> showOnly)
	{
		this.showOnly = showOnly;
	}

	public List<Plant> getPlants()
	{
		return plants;
	}

	public List<String> getShowOnly()
	{
		return showOnly;
	}

	public Unit getMeasureUnit()
	{
		return measureUnit;
	}

	public Unit getDeliveryUnit()
	{
		return deliveryUnit;
	}

	public PlantAdapter(Context context)
	{
		measureUnit = Unit.getSelectedMeasurementUnit(context);
		deliveryUnit = Unit.getSelectedDeliveryUnit(context);

		try
		{
			cardStyle = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("card_style", "1")) + 1;
		}
		catch (NumberFormatException e)
		{
			cardStyle = 1;
		}
	}

	public void setPlants(List<Plant> plants)
	{
		this.plants.clear();
		this.plants.addAll(plants);
		this.plants.removeAll(Collections.singleton(null));
	}

	@Override public int getItemViewType(int position)
	{
		Plant plant = plants.get(position);
		if (showOnly != null && !showOnly.contains(plant.getId()))
		{
			return 0;
		}

		return cardStyle;
	}

	public int getFilteredCount()
	{
		int total = 0;
		for (int position = 0; position < plants.size(); position++)
		{
			Plant plant = plants.get(position);
			if (showOnly == null || showOnly.contains(plant.getId()))
			{
				total++;
			}
		}

		return total;
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeView)
	{
		switch (typeView)
		{
			case 0:
				return new RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty, viewGroup, false)){};

			case 1:
				return new PlantHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plant_compact_item, viewGroup, false));

			case 3:
				return new PlantHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plant_extreme_item, viewGroup, false));

			default:
			case 2:
				return new PlantHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plant_original_item, viewGroup, false));
		}
	}

	@Override public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
	{
		final Plant plant = plants.get(position);

		if (viewHolder instanceof PlantHolder)
		{
			((PlantHolder)viewHolder).bind(plant, cardStyle);
		}
	}

	@Override public int getItemCount()
	{
		return plants.size();
	}

	@Override public void onItemMove(int fromPosition, int toPosition)
	{

	}

	@Override public void onItemDismiss(int position)
	{
		plants.remove(position);
		notifyItemRemoved(position);
	}
}
