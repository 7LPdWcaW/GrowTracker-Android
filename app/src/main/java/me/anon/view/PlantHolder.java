package me.anon.view;

import android.content.Intent;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import me.anon.grow.MainApplication;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
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
	private TextView shortSummary;
	private Button feed;
	private Button photo;
	private View overflow;

	public PlantHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		name = (TextView)itemView.findViewById(R.id.name);
		summary = (TextView)itemView.findViewById(R.id.summary);
		shortSummary = (TextView)itemView.findViewById(R.id.short_summary);
		feed = (Button)itemView.findViewById(R.id.action_feed);
		photo = (Button)itemView.findViewById(R.id.action_photo);
		overflow = itemView.findViewById(R.id.action_overflow);
	}

	public void bind(Plant plant)
	{
		name.setText(plant.getName());

		if (summary != null)
		{
			String summaryStr = plant.generateLongSummary(itemView.getContext());
			summary.setText(Html.fromHtml(summaryStr));
		}

		if (shortSummary != null)
		{
			String summaryStr = plant.generateShortSummary(itemView.getContext());
			shortSummary.setText(Html.fromHtml(summaryStr));
		}

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

		if (photo != null)
		{
			photo.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					Intent photos = new Intent(view.getContext(), PlantDetailsActivity.class);
					photos.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));
					photos.putExtra("forward", "photo");
					view.getContext().startActivity(photos);
				}
			});
		}

		if (feed != null)
		{
			feed.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					Intent photos = new Intent(view.getContext(), PlantDetailsActivity.class);
					photos.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));
					photos.putExtra("forward", "feed");
					view.getContext().startActivity(photos);
				}
			});
		}

		if (overflow != null)
		{
			overflow.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					PopupMenu menu = new PopupMenu(view.getContext(), view, Gravity.BOTTOM);
					menu.inflate(R.menu.plant_overflow);
					menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
					{
						@Override public boolean onMenuItemClick(MenuItem item)
						{
							Intent photos = new Intent(view.getContext(), PlantDetailsActivity.class);
							photos.putExtra("plant_index", PlantManager.getInstance().getPlants().indexOf(plant));

							if (item.getItemId() == R.id.menu_action)
							{
								photos.putExtra("forward", "action");
							}
							else if (item.getItemId() == R.id.menu_note)
							{
								photos.putExtra("forward", "note");
							}
							else if (item.getItemId() == R.id.menu_photos)
							{
								photos.putExtra("forward", "photos");
							}
							else if (item.getItemId() == R.id.menu_history)
							{
								photos.putExtra("forward", "history");
							}
							else if (item.getItemId() == R.id.menu_statistics)
							{
								photos.putExtra("forward", "statistics");
							}
							else
							{
								return false;
							}

							view.getContext().startActivity(photos);

							return true;
						}
					});

					menu.show();
				}
			});
		}
	}
}
