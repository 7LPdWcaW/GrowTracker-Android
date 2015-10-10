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
@Views.Injectable
public class ImageHolder extends RecyclerView.ViewHolder
{
	@Views.InjectView(R.id.image) private ImageView image;
	@Views.InjectView(R.id.selection) private CheckBox selection;

	public ImageHolder(View itemView)
	{
		super(itemView);
		Views.inject(this, itemView);
	}
}
