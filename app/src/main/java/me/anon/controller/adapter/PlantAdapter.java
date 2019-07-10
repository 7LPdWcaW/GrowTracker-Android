package me.anon.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.anon.grow.MainApplication;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.lib.Unit;
import me.anon.lib.manager.PlantManager;
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
	private Context context;
	private Unit measureUnit, deliveryUnit;

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
		this.context = context;

		measureUnit = Unit.getSelectedMeasurementUnit(context);
		deliveryUnit = Unit.getSelectedDeliveryUnit(context);
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

		return 1;
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeView)
	{
		switch (typeView)
		{
			case 0:
				return new RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty, viewGroup, false)){};

			default:
			case 1:
				return new PlantHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plant_item, viewGroup, false));
		}
	}

	@Override public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
	{
		final Plant plant = plants.get(position);

		if (getItemViewType(position) == 1)
		{
			((PlantHolder)viewHolder).getName().setText(plant.getName());

			String summary = plant.generateLongSummary(((PlantHolder)viewHolder).itemView.getContext());
			((PlantHolder)viewHolder).getSummary().setText(Html.fromHtml(summary));

			if (plant.getImages() != null && plant.getImages().size() > 0)
			{
				String imagePath = "file://" + plant.getImages().get(plant.getImages().size() - 1);

				if (((PlantHolder)viewHolder).getImage().getTag() == null || !((PlantHolder)viewHolder).getImage().getTag().toString().equalsIgnoreCase(imagePath))
				{
					ImageLoader.getInstance().cancelDisplayTask(((PlantHolder)viewHolder).getImage());
				}

				((PlantHolder)viewHolder).getImage().setTag(imagePath);

				ImageAware imageAware = new ImageViewAware(((PlantHolder)viewHolder).getImage(), true);
				ImageLoader.getInstance().displayImage("file://" + plant.getImages().get(plant.getImages().size() - 1), imageAware, MainApplication.getDisplayImageOptions());
			}
			else
			{
				((PlantHolder)viewHolder).getImage().setImageResource(R.drawable.default_plant);
			}

			viewHolder.itemView.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					Intent details = new Intent(v.getContext(), PlantDetailsActivity.class);
					details.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));
					v.getContext().startActivity(details);
				}
			});
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
