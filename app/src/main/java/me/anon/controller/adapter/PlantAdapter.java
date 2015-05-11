package me.anon.controller.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.anon.grow.MainApplication;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Feed;
import me.anon.model.Plant;
import me.anon.model.PlantStage;
import me.anon.model.Water;
import me.anon.view.PlantHolder;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
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

		String summary = "";

		summary += plant.getStrain() + " - " + plant.getStage();

		if (plant.getStage() == PlantStage.VEGETATION || plant.getStage() == PlantStage.GERMINATION)
		{
			summary += " (" + new DateRenderer().timeAgo(plant.getPlantDate()).formattedDate + ")";
		}

		if (plant.getActions() != null && plant.getActions().size() > 0)
		{
			Feed lastFeed = null;
			Water lastWater = null;

			ArrayList<Action> actions = plant.getActions();
			for (int index = actions.size() - 1; index >= 0; index--)
			{
				Action action = actions.get(index);

				if (action instanceof EmptyAction && ((EmptyAction)action).getAction() == Action.ActionName.FLIPPED)
				{
					long flipDate = action.getDate();
					summary += " (" + new DateRenderer().timeAgo(flipDate).formattedDate + ")";

					continue;
				}

				if (action instanceof Feed)
				{
					lastFeed = (Feed)action;
					break;
				}
				else if (action instanceof Water)
				{
					lastWater = (Water)action;
					break;
				}
			}

			if (lastFeed != null && lastFeed.getNutrient() != null)
			{
				summary += "\n";
				summary += "Last fed: " + new DateRenderer().timeAgo(lastFeed.getDate()).formattedDate + " ago with ";

				if (lastFeed.getMlpl() != null)
				{
					summary += lastFeed.getMlpl() + "ml/l of ";
				}

				summary += lastFeed.getNutrient().getNpc() == null ? "-" : lastFeed.getNutrient().getNpc();
				summary += " : ";
				summary += lastFeed.getNutrient().getPpc() == null ? "-" : lastFeed.getNutrient().getPpc();
				summary += " : ";
				summary += lastFeed.getNutrient().getKpc() == null ? "-" : lastFeed.getNutrient().getKpc();
				summary += "\n";

				if (lastFeed.getPh() != null)
				{
					summary += lastFeed.getPh() + " PH";
				}

				if (lastFeed.getPh() != null || lastFeed.getRunoff() != null)
				{
					summary += lastFeed.getPh() != null ? " -> " : "";
					summary += lastFeed.getRunoff() + " PH ";
				}

				if (lastFeed.getAmount() != null)
				{
					summary += lastFeed.getAmount() + "ml";
				}
			}
			else if (lastWater != null)
			{
				summary += "\n";
				summary += "Last watered: " + new DateRenderer().timeAgo(lastWater.getDate()).formattedDate + " ago";
				summary += "\n";

				if (lastWater.getPh() != null)
				{
					summary += lastWater.getPh() + " PH";
				}

				if (lastWater.getPh() != null || lastWater.getRunoff() != null)
				{
					summary += lastWater.getPh() != null ? " -> " : "";
					summary += lastWater.getRunoff() + " PH ";
				}

				if (lastWater.getAmount() != null)
				{
					summary += lastWater.getAmount() + "ml";
				}
			}
		}

		viewHolder.getSummary().setText(summary);

		ImageLoader.getInstance().cancelDisplayTask(viewHolder.getImage());
		if (plant.getImages() != null && plant.getImages().size() > 0)
		{
			ImageLoader.getInstance().displayImage("file://" + plant.getImages().get(plant.getImages().size() - 1), viewHolder.getImage(), MainApplication.getDisplayImageOptions());
		}
		else
		{
			viewHolder.getImage().setImageResource(R.drawable.default_plant);
		}

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
