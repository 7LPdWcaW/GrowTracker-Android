package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.anon.grow.R;

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

	public ImageView getImage()
	{
		return image;
	}

	public TextView getName()
	{
		return name;
	}

	public TextView getSummary()
	{
		return summary;
	}

	public PlantHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		name = (TextView)itemView.findViewById(R.id.name);
		summary = (TextView)itemView.findViewById(R.id.summary);
	}
}
