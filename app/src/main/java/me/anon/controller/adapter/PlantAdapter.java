package me.anon.controller.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
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
	@Getter private List<Plant> plants = new ArrayList<>();

	public void setPlants(List<Plant> plants)
	{
		this.plants.clear();
		this.plants.addAll(plants);
		Collections.reverse(this.plants);
	}

	@Override public PlantHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{
		return new PlantHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plant_item, viewGroup, false));
	}

	@Override public void onBindViewHolder(PlantHolder viewHolder, final int i)
	{
		final Plant plant = plants.get(i);
		viewHolder.getName().setText(plant.getName());

		String summary = "";

		summary += plant.getStrain() + " - ";
		summary += "Planted (" + new DateRenderer().timeAgo(plant.getPlantDate(), 3).formattedDate + " ago)";

		if (plant.getActions() != null && plant.getActions().size() > 0)
		{
			Feed lastFeed = null;
			Water lastWater = null;

			ArrayList<Action> actions = plant.getActions();
			for (int index = actions.size() - 1; index >= 0; index--)
			{
				Action action = actions.get(index);

				if (action instanceof EmptyAction && ((EmptyAction)action).getAction() == Action.ActionName.FLIPPED && plant.getStage() == PlantStage.FLOWER)
				{
					long flipDate = action.getDate();
					String time = new DateRenderer().timeAgo(flipDate, 3).formattedDate;
					summary += " / (" + time.replaceAll("[^0-9]", "") + "f)";
				}

				if (action instanceof Feed && lastFeed == null)
				{
					lastFeed = (Feed)action;
				}

				if (action instanceof Water && lastWater == null)
				{
					lastWater = (Water)action;
				}
			}

			if (lastFeed != null && lastFeed.getNutrient() != null)
			{
				summary += "<br/><br/>";
				summary += "Last fed: <b>" + new DateRenderer().timeAgo(lastFeed.getDate()).formattedDate + "</b> ago with ";

				if (lastFeed.getMlpl() != null)
				{
					summary += "<b>" + lastFeed.getMlpl() + "ml/l</b> of ";
				}

				summary += "<b>";
				summary += lastFeed.getNutrient().getNpc() == null ? "-" : lastFeed.getNutrient().getNpc();
				summary += " : ";
				summary += lastFeed.getNutrient().getPpc() == null ? "-" : lastFeed.getNutrient().getPpc();
				summary += " : ";
				summary += lastFeed.getNutrient().getKpc() == null ? "-" : lastFeed.getNutrient().getKpc();
				summary += "</b><br/>";

				if (lastFeed.getPh() != null)
				{
					summary += "<b>" + lastFeed.getPh() + " PH</b>";
				}

				if (lastFeed.getPh() != null || lastFeed.getRunoff() != null)
				{
					summary += lastFeed.getPh() != null ? " -> " : "";
					summary += "<b>" + lastFeed.getRunoff() + " PH</b> ";
				}

				if (lastFeed.getAmount() != null)
				{
					summary += "<b>" + lastFeed.getAmount() + "ml</b>";
				}
			}
			else if (lastWater != null)
			{
				summary += "<br/>";
				summary += "Last watered: <b>" + new DateRenderer().timeAgo(lastWater.getDate()).formattedDate + "</b> ago";
				summary += "<br/>";

				if (lastWater.getPh() != null)
				{
					summary += "<b>" + lastWater.getPh() + " PH</b>";
				}

				if (lastWater.getPh() != null || lastWater.getRunoff() != null)
				{
					summary += lastWater.getPh() != null ? " -> " : "";
					summary += "<b>" + lastWater.getRunoff() + " PH</b> ";
				}

				if (lastWater.getAmount() != null)
				{
					summary += "<b>" + lastWater.getAmount() + "ml</b>";
				}
			}
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length() - "<br/>".length());
		}

		viewHolder.getSummary().setText(Html.fromHtml(summary));

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
				details.putExtra("plant_index", plants.size() - i - 1);
				v.getContext().startActivity(details);
			}
		});
	}

	@Override public int getItemCount()
	{
		return plants.size();
	}
}
