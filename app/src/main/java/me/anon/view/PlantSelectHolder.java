package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import lombok.Data;
import me.anon.grow.R;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
public class PlantSelectHolder extends RecyclerView.ViewHolder
{
	private ImageView image;
	private CheckBox checkbox;
	private TextView name;

	public PlantSelectHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		checkbox = (CheckBox)itemView.findViewById(R.id.checkbox);
		name = (TextView)itemView.findViewById(R.id.name);
	}
}
