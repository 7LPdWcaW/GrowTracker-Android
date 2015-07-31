package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import lombok.Data;
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
public class PlantHolder extends RecyclerView.ViewHolder
{
	@Views.InjectView(R.id.image) private ImageView image;
	@Views.InjectView(R.id.name) private TextView name;
	@Views.InjectView(R.id.summary) private TextView summary;

	public PlantHolder(View itemView)
	{
		super(itemView);
		Views.inject(this, itemView);
	}
}
