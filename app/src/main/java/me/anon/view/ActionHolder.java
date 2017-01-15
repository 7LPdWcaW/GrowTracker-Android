package me.anon.view;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
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
public class ActionHolder extends RecyclerView.ViewHolder
{
	@Views.InjectView(R.id.card) private CardView card;
	@Views.InjectView(R.id.date) private TextView date;
	@Views.InjectView(R.id.full_date) private TextView fullDate;
	@Views.InjectView(R.id.date_day) private TextView dateDay;
	@Views.InjectView(R.id.name) private TextView name;
	@Views.InjectView(R.id.summary) private TextView summary;
	@Views.InjectView(R.id.overflow) private ImageButton overflow;

	public ActionHolder(View itemView)
	{
		super(itemView);
		Views.inject(this, itemView);
	}
}
