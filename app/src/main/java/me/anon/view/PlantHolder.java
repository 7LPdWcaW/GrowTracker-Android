package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class PlantHolder extends RecyclerView.ViewHolder
{
	public PlantHolder(View itemView)
	{
		super(itemView);
		Views.inject(this, itemView);
	}
}
