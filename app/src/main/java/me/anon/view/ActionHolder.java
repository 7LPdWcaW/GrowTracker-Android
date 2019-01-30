package me.anon.view;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import me.anon.grow.R;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class ActionHolder extends RecyclerView.ViewHolder
{
	private CardView card;
	private TextView date;
	private TextView fullDate;
	private TextView dateDay;
	private TextView stageDay;
	private TextView name;
	private TextView summary;
	private ImageButton overflow;

	public CardView getCard()
	{
		return card;
	}

	public TextView getDate()
	{
		return date;
	}

	public TextView getFullDate()
	{
		return fullDate;
	}

	public TextView getDateDay()
	{
		return dateDay;
	}

	public TextView getStageDay()
	{
		return stageDay;
	}

	public TextView getName()
	{
		return name;
	}

	public TextView getSummary()
	{
		return summary;
	}

	public ImageButton getOverflow()
	{
		return overflow;
	}

	public ActionHolder(View itemView)
	{
		super(itemView);

		card = (CardView)itemView.findViewById(R.id.card);
		date = (TextView)itemView.findViewById(R.id.date);
		fullDate = (TextView)itemView.findViewById(R.id.full_date);
		dateDay = (TextView)itemView.findViewById(R.id.date_day);
		stageDay = (TextView)itemView.findViewById(R.id.stage_day);
		name = (TextView)itemView.findViewById(R.id.name);
		summary = (TextView)itemView.findViewById(R.id.summary);
		overflow = (ImageButton)itemView.findViewById(R.id.overflow);
	}
}
