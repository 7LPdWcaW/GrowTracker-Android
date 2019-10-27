package me.anon.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import me.anon.grow.BuildConfig;
import me.anon.grow.MainApplication;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.model.Plant;
import me.anon.model.PlantStage;

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
	private TextView strain;
	private TextView summary;
	private Button feed;
	private Button photo;
	private View overflow;

	public PlantHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		name = (TextView)itemView.findViewById(R.id.name);
		strain = (TextView)itemView.findViewById(R.id.strain);
		summary = (TextView)itemView.findViewById(R.id.summary);
		feed = (Button)itemView.findViewById(R.id.action_feed);
		photo = (Button)itemView.findViewById(R.id.action_photo);
		overflow = itemView.findViewById(R.id.action_overflow);
	}

	public void bind(Plant plant, int cardStyle)
	{
		PlantStage stage = plant.getStage();

		if (feed != null) feed.setVisibility(stage == PlantStage.HARVESTED ? View.GONE : View.VISIBLE);
		if (photo != null) photo.setVisibility(stage == PlantStage.HARVESTED ? View.GONE : View.VISIBLE);
		if (overflow != null) overflow.setVisibility(stage == PlantStage.HARVESTED ? View.GONE : View.VISIBLE);

		ArrayList<String> summaryList = plant.generateSummary(itemView.getContext(), cardStyle);
		name.setText(plant.getName());

		if (cardStyle == 1)
		{
			if (plant.getStrain() != null)
			{
				summaryList.set(0, (plant.getStrain() + " " + summaryList.get(0)).trim());
			}
		}

		if (strain != null)
		{
			strain.setText(plant.getStrain());
		}

		summary.setText(Html.fromHtml(TextUtils.join("<br />", summaryList)));

		if (BuildConfig.DISCRETE)
		{
			View parent = null;
			if ((parent = itemView.findViewById(R.id.original_parent)) != null)
			{
				parent.setBackgroundColor(itemView.getResources().getColor(R.color.green));
			}

			image.setVisibility(View.GONE);
		}
		else
		{
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
		}

		itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				Intent details = new Intent(v.getContext(), PlantDetailsActivity.class);
				details.putExtra("plant", plant);
				((Activity)v.getContext()).startActivityForResult(details, 5);
			}
		});

		if (photo != null)
		{
			photo.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					Intent intent = new Intent(view.getContext(), PlantDetailsActivity.class);
					intent.putExtra("plant", plant);
					intent.putExtra("forward", "photo");
					((Activity)view.getContext()).startActivityForResult(intent, 5);
				}
			});
		}

		if (feed != null)
		{
			feed.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View view)
				{
					Intent intent = new Intent(view.getContext(), PlantDetailsActivity.class);
					intent.putExtra("plant", plant);
					intent.putExtra("forward", "feed");
					((Activity)view.getContext()).startActivityForResult(intent, 5);
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
							Intent intent = new Intent(view.getContext(), PlantDetailsActivity.class);

							if (item.getItemId() == R.id.menu_action)
							{
								intent.putExtra("forward", "action");
							}
							else if (item.getItemId() == R.id.menu_note)
							{
								intent.putExtra("forward", "note");
							}
							else if (item.getItemId() == R.id.menu_photos)
							{
								intent.putExtra("forward", "photos");
							}
							else if (item.getItemId() == R.id.menu_history)
							{
								intent.putExtra("forward", "events");
							}
							else if (item.getItemId() == R.id.menu_statistics)
							{
								intent.putExtra("forward", "statistics");
							}
							else
							{
								return false;
							}

							intent.putExtra("plant", plant);
							((Activity)view.getContext()).startActivityForResult(intent, 5);

							return true;
						}
					});

					menu.show();
				}
			});
		}
	}
}
