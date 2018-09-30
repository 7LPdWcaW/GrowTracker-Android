package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
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
public class PlantSelectHolder extends RecyclerView.ViewHolder
{
	private ImageView image;
	private CheckBox checkbox;
	private TextView name;

	public ImageView getImage()
	{
		return image;
	}

	public CheckBox getCheckbox()
	{
		return checkbox;
	}

	public TextView getName()
	{
		return name;
	}

	public PlantSelectHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		checkbox = (CheckBox)itemView.findViewById(R.id.checkbox);
		name = (TextView)itemView.findViewById(R.id.name);
	}
}
