package me.anon.controller.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
import me.anon.model.NoteAction;
import me.anon.model.StageChange;
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
	public interface OnActionSelectListener
	{
		public void onActionDeleted(Action action);
		public void onActionEdit(Action action);
	}

	@Setter private OnActionSelectListener onActionSelectListener;
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

		String dateStr = dateFormat.format(new Date(action.getDate())) + " " + timeFormat.format(new Date(action.getDate())) + " - <b>" + new DateRenderer().timeAgo(action.getDate()).formattedDate + "</b> ago";

		if (i > 0)
		{
			long difference = actions.get(i - 1).getDate() - action.getDate();
			int days = (int)Math.round(((double)difference / 60d / 60d / 24d / 1000d));

			dateStr += " (-" + days + "d)";
		}

		viewHolder.getDate().setText(Html.fromHtml(dateStr));
		viewHolder.getSummary().setVisibility(View.GONE);

		viewHolder.itemView.setBackgroundColor(0xffffffff);

		String summary = "";
		if (action instanceof Feed)
		{
			viewHolder.itemView.setBackgroundColor(0x9A90CAF9);
			viewHolder.getName().setText("Feed with nutrients");

			if (((Feed)action).getNutrient() != null)
			{
				summary += ((Feed)action).getNutrient().getNpc() == null ? "-" : ((Feed)action).getNutrient().getNpc();
				summary += " : ";
				summary += ((Feed)action).getNutrient().getPpc() == null ? "-" : ((Feed)action).getNutrient().getPpc();
				summary += " : ";
				summary += ((Feed)action).getNutrient().getKpc() == null ? "-" : ((Feed)action).getNutrient().getKpc();
				summary += "/";
				summary += ((Feed)action).getNutrient().getCapc() == null ? "-" : ((Feed)action).getNutrient().getCapc();
				summary += " : ";
				summary += ((Feed)action).getNutrient().getSpc() == null ? "-" : ((Feed)action).getNutrient().getSpc();
				summary += " : ";
				summary += ((Feed)action).getNutrient().getMgpc() == null ? "-" : ((Feed)action).getNutrient().getMgpc();
				summary += " (";
				summary += ((Feed)action).getMlpl() == null ? "n/a" : ((Feed)action).getMlpl() + "ml/l";
				summary += ")";
				summary += "\n";
			}

			StringBuilder waterStr = new StringBuilder();

			if (((Feed)action).getPh() != null)
			{
				waterStr.append("PH: ");
				waterStr.append(((Feed)action).getPh());
				waterStr.append(", ");
			}

			if (((Feed)action).getRunoff() != null)
			{
				waterStr.append("Runoff: ");
				waterStr.append(((Feed)action).getRunoff());
				waterStr.append(", ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) + "\n" : "";

			waterStr = new StringBuilder();

			if (((Feed)action).getPpm() != null)
			{
				waterStr.append("PPM: ");
				waterStr.append(((Feed)action).getPpm());
				waterStr.append(", ");
			}

			if (((Feed)action).getAmount() != null)
			{
				waterStr.append("Amount: ");
				waterStr.append(((Feed)action).getAmount());
				waterStr.append("ml, ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) : "";
		}
		else if (action instanceof Water)
		{
			viewHolder.itemView.setBackgroundColor(0x9ABBDEFB);
			viewHolder.getName().setText("Watered");
			StringBuilder waterStr = new StringBuilder();

			if (((Water)action).getPh() != null)
			{
				waterStr.append("PH: ");
				waterStr.append(((Water)action).getPh());
				waterStr.append(", ");
			}

			if (((Water)action).getRunoff() != null)
			{
				waterStr.append("Runoff: ");
				waterStr.append(((Water)action).getRunoff());
				waterStr.append(", ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) + "\n" : "";

			waterStr = new StringBuilder();

			if (((Water)action).getPpm() != null)
			{
				waterStr.append("PPM: ");
				waterStr.append(((Water)action).getPpm());
				waterStr.append(", ");
			}

			if (((Water)action).getAmount() != null)
			{
				waterStr.append("Amount: ");
				waterStr.append(((Water)action).getAmount());
				waterStr.append("ml, ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) : "";
		}
		else if (action instanceof EmptyAction && ((EmptyAction)action).getAction() != null)
		{
			viewHolder.getName().setText(((EmptyAction)action).getAction().getPrintString());
			viewHolder.itemView.setBackgroundColor(((EmptyAction)action).getAction().getColour());

			summary = action.getNotes();
		}
		else if (action instanceof NoteAction)
		{
			viewHolder.getName().setText("Note");
			viewHolder.itemView.setBackgroundColor(0xffffffff);

			summary = action.getNotes();
		}
		else if (action instanceof StageChange)
		{
			viewHolder.getName().setText(((StageChange)action).getNewStage().getPrintString());
			viewHolder.itemView.setBackgroundColor(0x9AB39DDB);
		}

		if (!TextUtils.isEmpty(summary))
		{
			viewHolder.getSummary().setText(summary);
			viewHolder.getSummary().setVisibility(View.VISIBLE);
		}

		viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override public boolean onLongClick(final View v)
			{
				new AlertDialog.Builder(v.getContext())
					.setTitle("Select an option")
					.setItems(new String[]{"Edit action", "Delete action"}, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							if (which == 0)
							{
								if (onActionSelectListener != null)
								{
									onActionSelectListener.onActionEdit(action);
								}
							}
							else if (which == 1)
							{
								new AlertDialog.Builder(v.getContext())
									.setTitle("Delete this event?")
									.setMessage("Are you sure you want to delete " + viewHolder.getName().getText())
									.setPositiveButton("Yes", new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which)
										{
											if (onActionSelectListener != null)
											{
												onActionSelectListener.onActionDeleted(action);
											}
										}
									})
									.setNegativeButton("No", null)
									.show();
							}
						}
					})
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
