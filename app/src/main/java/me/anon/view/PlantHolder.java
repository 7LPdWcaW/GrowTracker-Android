package me.anon.view;

import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import androidx.recyclerview.widget.RecyclerView;
import me.anon.grow.EventsActivity;
import me.anon.grow.MainApplication;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.grow.StatisticsActivity;
import me.anon.grow.ViewPhotosActivity;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class PlantHolder extends RecyclerView.ViewHolder
{
	private ImageView image;
	private TextView name;
	private TextView summary;
	private Button photos;
	private Button history;
	private Button statistic;

	public PlantHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		name = (TextView)itemView.findViewById(R.id.name);
		summary = (TextView)itemView.findViewById(R.id.summary);
		photos = (Button)itemView.findViewById(R.id.view_photos);
		history = (Button)itemView.findViewById(R.id.view_history);
		statistic = (Button)itemView.findViewById(R.id.view_statistics);
	}

	public void bind(Plant plant)
	{
		name.setText(plant.getName());

		String summaryStr = plant.generateLongSummary(itemView.getContext());
		summary.setText(Html.fromHtml(summaryStr));

		if (plant.getImages() != null && plant.getImages().size() > 0)
		{
			String imagePath = "file://" + plant.getImages().get(plant.getImages().size() - 1);

			if (image.getTag() == null || !image.getTag().toString().equalsIgnoreCase(imagePath))
			{
				ImageLoader.getInstance().cancelDisplayTask(image);
			}

			image.setTag(imagePath);

			ImageAware imageAware = new ImageViewAware(image, true);
			ImageLoader.getInstance().displayImage("file://" + plant.getImages().get(plant.getImages().size() - 1), imageAware, MainApplication.getDisplayImageOptions());
		}
		else
		{
			image.setImageResource(R.drawable.default_plant);
		}

		itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				Intent details = new Intent(v.getContext(), PlantDetailsActivity.class);
				details.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));
				v.getContext().startActivity(details);
			}
		});

		if (photos != null)
		{
			photos.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					Intent photos = new Intent(view.getContext(), ViewPhotosActivity.class);
					photos.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));
					view.getContext().startActivity(photos);
				}
			});
		}

		if (statistic != null)
		{
			statistic.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					Intent photos = new Intent(view.getContext(), StatisticsActivity.class);
					photos.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));
					view.getContext().startActivity(photos);
				}
			});
		}

		if (history != null)
		{
			history.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					Intent photos = new Intent(view.getContext(), EventsActivity.class);
					photos.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));
					view.getContext().startActivity(photos);
				}
			});
		}
	}
}
