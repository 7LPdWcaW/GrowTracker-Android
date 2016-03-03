package me.anon.controller.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.lib.helper.ModelHelper;
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
public class ActionAdapter extends RecyclerView.Adapter<ActionHolder> implements ItemTouchHelperAdapter
{
	public interface OnActionSelectListener
	{
		public void onActionDeleted(Action action);
		public void onActionEdit(Action action);
		public void onActionCopy(Action action);
		public void onActionDuplicate(Action action);
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

		if (action == null) return;

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(viewHolder.getDate().getContext());
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(viewHolder.getDate().getContext());

		String fullDateStr = dateFormat.format(new Date(action.getDate())) + " " + timeFormat.format(new Date(action.getDate()));
		String dateStr = "<b>" + new DateRenderer().timeAgo(action.getDate()).formattedDate + "</b> ago";

		if (i > 0)
		{
			long difference = actions.get(i - 1).getDate() - action.getDate();
			int days = (int)Math.round(((double)difference / 60d / 60d / 24d / 1000d));

			dateStr += " (-" + days + "d)";
		}

		viewHolder.getFullDate().setText(Html.fromHtml(fullDateStr));
		viewHolder.getDate().setText(Html.fromHtml(dateStr));
		viewHolder.getSummary().setVisibility(View.GONE);

		viewHolder.getCard().setCardBackgroundColor(0xffffffff);

		String summary = "";
		if (action.getClass() == Feed.class)
		{
			viewHolder.getCard().setCardBackgroundColor(0x9A90CAF9);
			viewHolder.getName().setText("Feed with nutrients");

			if (((Feed)action).getNutrient() != null)
			{
				summary += "<b>";
				summary += ((Feed)action).getNutrient().getNpc() == null ? "-" : ((Feed)action).getNutrient().getNpc();
				summary += "</b>:<b>";
				summary += ((Feed)action).getNutrient().getPpc() == null ? "-" : ((Feed)action).getNutrient().getPpc();
				summary += "</b>:<b>";
				summary += ((Feed)action).getNutrient().getKpc() == null ? "-" : ((Feed)action).getNutrient().getKpc();
				summary += "</b>";

				if (((Feed)action).getNutrient().getMgpc() != null
				|| ((Feed)action).getNutrient().getSpc() != null
				|| ((Feed)action).getNutrient().getCapc() != null)
				{
					summary += " / <b>";
					summary += ((Feed)action).getNutrient().getCapc() == null ? "-" : ((Feed)action).getNutrient().getCapc();
					summary += "</b>:<b>";
					summary += ((Feed)action).getNutrient().getSpc() == null ? "-" : ((Feed)action).getNutrient().getSpc();
					summary += "</b>:<b>";
					summary += ((Feed)action).getNutrient().getMgpc() == null ? "-" : ((Feed)action).getNutrient().getMgpc();
					summary += "</b>";
				}

				summary += " (";
				summary += ((Feed)action).getMlpl() == null ? "n/a" : ((Feed)action).getMlpl() + "ml/l";
				summary += ")";
				summary += "<br/>";
			}

			StringBuilder waterStr = new StringBuilder();

			if (((Feed)action).getPh() != null)
			{
				waterStr.append("<b>In pH: </b>");
				waterStr.append(((Feed)action).getPh());
				waterStr.append(", ");
			}

			if (((Feed)action).getRunoff() != null)
			{
				waterStr.append("<b>Out pH: </b>");
				waterStr.append(((Feed)action).getRunoff());
				waterStr.append(", ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) + "<br/>" : "";

			waterStr = new StringBuilder();

			if (((Feed)action).getPpm() != null)
			{
				waterStr.append("<b>PPM: </b>");
				waterStr.append(((Feed)action).getPpm());
				waterStr.append(", ");
			}

			if (((Feed)action).getAmount() != null)
			{
				waterStr.append("<b>Amount: </b>");
				waterStr.append(((Feed)action).getAmount());
				waterStr.append("ml, ");
			}

			if (((Feed)action).getTemp() != null)
			{
				waterStr.append("<b>Temp: </b>");
				waterStr.append(((Feed)action).getTemp());
				waterStr.append("ºC, ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) : "";
		}
		else if (action.getClass() == Water.class)
		{
			viewHolder.getCard().setCardBackgroundColor(0x9ABBDEFB);
			viewHolder.getName().setText("Watered");
			StringBuilder waterStr = new StringBuilder();

			if (((Water)action).getPh() != null)
			{
				waterStr.append("<b>In pH: </b>");
				waterStr.append(((Water)action).getPh());
				waterStr.append(", ");
			}

			if (((Water)action).getRunoff() != null)
			{
				waterStr.append("<b>Out pH: </b>");
				waterStr.append(((Water)action).getRunoff());
				waterStr.append(", ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) + "<br/>" : "";

			waterStr = new StringBuilder();

			if (((Water)action).getPpm() != null)
			{
				waterStr.append("<b>PPM: </b>");
				waterStr.append(((Water)action).getPpm());
				waterStr.append(", ");
			}

			if (((Water)action).getAmount() != null)
			{
				waterStr.append("<b>Amount: </b>");
				waterStr.append(((Water)action).getAmount());
				waterStr.append("ml, ");
			}

			if (((Water)action).getTemp() != null)
			{
				waterStr.append("<b>Temp: </b>");
				waterStr.append(((Water)action).getTemp());
				waterStr.append("ºC, ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) : "";
		}
		else if (action instanceof EmptyAction && ((EmptyAction)action).getAction() != null)
		{
			viewHolder.getName().setText(((EmptyAction)action).getAction().getPrintString());
			viewHolder.getCard().setCardBackgroundColor(((EmptyAction)action).getAction().getColour());
		}
		else if (action instanceof NoteAction)
		{
			viewHolder.getName().setText("Note");
			viewHolder.getCard().setCardBackgroundColor(0xffffffff);
		}
		else if (action instanceof StageChange)
		{
			viewHolder.getName().setText(((StageChange)action).getNewStage().getPrintString());
			viewHolder.getCard().setCardBackgroundColor(0x9AB39DDB);
		}

		if (!TextUtils.isEmpty(action.getNotes()))
		{
			summary += summary.length() > 0 ? "<br/><br/>" : "";
			summary += action.getNotes();
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length() - "<br/>".length());
		}

		if (!TextUtils.isEmpty(summary))
		{
			viewHolder.getSummary().setText(Html.fromHtml(summary));
			viewHolder.getSummary().setVisibility(View.VISIBLE);
		}

		viewHolder.getOverflow().setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(final View v)
			{
				PopupMenu menu = new PopupMenu(v.getContext(), v, Gravity.BOTTOM);
				menu.inflate(R.menu.event_overflow);
				menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{
					@Override public boolean onMenuItemClick(MenuItem item)
					{
						if (item.getItemId() == R.id.duplicate)
						{
							if (onActionSelectListener != null)
							{
								onActionSelectListener.onActionDuplicate((Action)ModelHelper.copy(action));
							}

							return true;
						}
						else if (item.getItemId() == R.id.copy)
						{
							if (onActionSelectListener != null)
							{
								onActionSelectListener.onActionCopy((Action)ModelHelper.copy(action));
							}

							return true;
						}
						else if (item.getItemId() == R.id.edit)
						{
							if (onActionSelectListener != null)
							{
								onActionSelectListener.onActionEdit(action);
							}

							return true;
						}
						else if (item.getItemId() == R.id.delete)
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

							return true;
						}

						return false;
					}
				});

				menu.show();
			}
		});
	}

	@Override public int getItemCount()
	{
		return actions.size();
	}

	@Override public void onItemMove(int fromPosition, int toPosition)
	{
		if (fromPosition < toPosition)
		{
			for (int index = fromPosition; index < toPosition; index++)
			{
				Collections.swap(actions, index, index + 1);
			}
		}
		else
		{
			for (int index = fromPosition; index > toPosition; index--)
			{
				Collections.swap(actions, index, index - 1);
			}
		}

		notifyItemMoved(fromPosition, toPosition);
	}

	@Override public void onItemDismiss(int position)
	{
		actions.remove(position);
		notifyItemRemoved(position);
	}
}
