package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import me.anon.grow.R;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class ImageHolder extends RecyclerView.ViewHolder
{
	private ImageView image;
	private CheckBox selection;

	public ImageView getImage()
	{
		return image;
	}

	public CheckBox getSelection()
	{
		return selection;
	}

	public ImageHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		selection = (CheckBox)itemView.findViewById(R.id.selection);
	}
}
