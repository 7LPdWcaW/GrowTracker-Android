package me.anon.controller.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Feed;
import me.anon.model.Water;
import me.anon.view.ActionHolder;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class ActionAdapter extends RecyclerView.Adapter<ActionHolder>
{
	public interface OnActionDeletedListener
	{
		public void onActionDeleted(Action action);
	}

	@Setter private OnActionDeletedListener onActionDeletedListener;
	@Getter @Setter private List<Action> actions = new ArrayList<>();

	@Override public ActionHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{
		return new ActionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.action_item, viewGroup, false));
	}

	@Override public void onBindViewHolder(final ActionHolder viewHolder, final int i)
	{
		final Action action = actions.get(i);

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(viewHolder.getDate().getContext());
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(viewHolder.getDate().getContext());
		viewHolder.getDate().setText(dateFormat.format(new Date(action.getDate())) + " " + timeFormat.format(new Date(action.getDate())) + " - " + new DateRenderer().timeAgo(action.getDate()).formattedDate + " ago");
		viewHolder.getSummary().setVisibility(View.GONE);

		String summary = "";
		if (action instanceof Feed)
		{
			viewHolder.getName().setText("Feed with nutrients");

			if (((Feed)action).getNutrient() != null)
			{
				summary += ((Feed)action).getNutrient().getNpc() + " : " + ((Feed)action).getNutrient().getPpc() + " : " + ((Feed)action).getNutrient().getKpc();
				summary += "/";
				summary += ((Feed)action).getNutrient().getCapc() + " : " + ((Feed)action).getNutrient().getSpc() + " : " + ((Feed)action).getNutrient().getMgpc();
				summary += " (" + ((Feed)action).getMlpl() + "ml/l)";
				summary += "\n";
			}

			summary += "PH: " + ((Feed)action).getPh() + ", Runoff: " + ((Feed)action).getRunoff();
			summary += "\n";
			summary += "PPM: " + ((Feed)action).getPpm() + ", Amount: " + ((Feed)action).getAmount() + "ml";
		}
		else if (action instanceof Water)
		{
			viewHolder.getName().setText("Watered");
			summary += "PH: " + ((Water)action).getPh() + ", Runoff: " + ((Water)action).getRunoff();
			summary += "\n";
			summary += "PPM: " + ((Water)action).getPpm() + ", Amount: " + ((Water)action).getAmount() + "ml";
		}
		else if (action instanceof EmptyAction && ((EmptyAction)action).getAction() != null)
		{
			viewHolder.getName().setText(((EmptyAction)action).getAction().getPrintString());
		}

		if (!TextUtils.isEmpty(summary))
		{
			viewHolder.getSummary().setText(summary);
			viewHolder.getSummary().setVisibility(View.VISIBLE);
		}

		viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override public boolean onLongClick(View v)
			{
				new AlertDialog.Builder(v.getContext())
					.setTitle("Delete this event?")
					.setMessage("Are you sure you want to delete " + viewHolder.getName().getText())
					.setPositiveButton("Yes", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							if (onActionDeletedListener != null)
							{
								onActionDeletedListener.onActionDeleted(action);
							}
						}
					})
					.setNegativeButton("No", null)
					.show();

				return true;
			}
		});
	}

	@Override public int getItemCount()
	{
		return actions.size();
	}
}
