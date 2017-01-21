package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import lombok.Data;
import lombok.Getter;
import me.anon.grow.R;
import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
public class ImageHolder extends RecyclerView.ViewHolder
{
	private ImageView image;
	private CheckBox selection;

	public ImageHolder(View itemView)
	{
		super(itemView);

		image = (ImageView)itemView.findViewById(R.id.image);
		selection = (CheckBox)itemView.findViewById(R.id.selection);
	}
}
