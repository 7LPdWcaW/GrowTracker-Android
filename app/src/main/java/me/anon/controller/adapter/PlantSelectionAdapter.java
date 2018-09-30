package me.anon.controller.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;

import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.model.Plant;
import me.anon.view.PlantSelectHolder;

public class PlantSelectionAdapter extends RecyclerView.Adapter<PlantSelectHolder>
{
	private ArrayList<Plant> plants = new ArrayList<>();
	private ArrayList<String> selectedIds = new ArrayList<>();
	private Context context;

	public ArrayList<Plant> getPlants()
	{
		return plants;
	}

	public ArrayList<String> getSelectedIds()
	{
		return selectedIds;
	}

	public PlantSelectionAdapter(@Nullable ArrayList<Plant> plants, @Nullable ArrayList<String> selectedIds, Context context)
	{
		this.plants = plants;
		this.selectedIds = selectedIds;
		this.context = context;

		if (this.plants == null)
		{
			this.plants = new ArrayList<>();
		}

		if (this.selectedIds == null)
		{
			this.selectedIds = new ArrayList<>();
		}
	}

	public void setSelectedIds(ArrayList<String> selectedIds)
	{
		this.selectedIds.clear();
		this.selectedIds.addAll(selectedIds);
	}

	@Override public PlantSelectHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_select_item, parent, false);
		return new PlantSelectHolder(view);
	}

	@Override public void onBindViewHolder(PlantSelectHolder holder, int position)
	{
		final Plant plant = plants.get(position);
		boolean selected = selectedIds.contains(plants.get(position).getId());

		holder.getCheckbox().setChecked(selected);
		holder.itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View view)
			{
				boolean check = !((CheckBox)view.findViewById(R.id.checkbox)).isChecked();
				((CheckBox)view.findViewById(R.id.checkbox)).setChecked(check);

				if (check)
				{
					selectedIds.add(plant.getId());
				}
				else
				{
					selectedIds.remove(plant.getId());
				}
			}
		});
		holder.getName().setText(plant.getName());

		if (plant.getImages() != null && plant.getImages().size() > 0)
		{
			String imagePath = "file://" + plant.getImages().get(plant.getImages().size() - 1);

			if (holder.getImage().getTag() == null || !holder.getImage().getTag().toString().equalsIgnoreCase(imagePath))
			{
				ImageLoader.getInstance().cancelDisplayTask(holder.getImage());
			}

			holder.getImage().setTag(imagePath);
			ImageAware imageAware = new ImageViewAware(holder.getImage(), true);
			ImageLoader.getInstance().displayImage(imagePath, imageAware, MainApplication.getDisplayImageOptions());
		}
		else
		{
			holder.getImage().setImageResource(R.drawable.default_plant);
		}
	}

	@Override public int getItemCount()
	{
		return plants.size();
	}
}
