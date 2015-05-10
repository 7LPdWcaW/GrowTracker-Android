package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import lombok.Data;
import me.anon.grow.R;
import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Views.Injectable
public class ImageHolder extends RecyclerView.ViewHolder
{
	@Views.InjectView(R.id.image) private ImageView image;

	public ImageHolder(View itemView)
	{
		super(itemView);
		Views.inject(this, itemView);
	}
}
