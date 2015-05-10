package me.anon.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

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
public class ActionHolder extends RecyclerView.ViewHolder
{
	@Views.InjectView(R.id.date) private TextView date;
	@Views.InjectView(R.id.name) private TextView name;
	@Views.InjectView(R.id.summary) private TextView summary;

	public ActionHolder(View itemView)
	{
		super(itemView);
		Views.inject(this, itemView);
	}
}
