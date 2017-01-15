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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.lib.Unit;
import me.anon.lib.helper.ModelHelper;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.StageChange;
import me.anon.model.Water;
import me.anon.view.ActionHolder;

import static me.anon.lib.Unit.ML;

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
	@Getter private List<Action> actions = new ArrayList<>();
	@Getter private Unit measureUnit, deliveryUnit;
	@Setter private String lastDateStr = "";

	public void setActions(List<Action> actions)
	{
		this.actions = actions;
		lastDateStr = "";
	}

	@Override public ActionHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{
		return new ActionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.action_item, viewGroup, false));
	}

	@Override public void onBindViewHolder(final ActionHolder viewHolder, final int i)
	{
		final Action action = actions.get(i);

		if (measureUnit == null)
		{
			measureUnit = Unit.getSelectedMeasurementUnit(viewHolder.itemView.getContext());
		}

		if (deliveryUnit == null)
		{
			deliveryUnit = Unit.getSelectedDeliveryUnit(viewHolder.itemView.getContext());
		}

		if (action == null) return;

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(viewHolder.getDate().getContext());
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(viewHolder.getDate().getContext());

		Date actionDate = new Date(action.getDate());
		Calendar actionCalendar = GregorianCalendar.getInstance();
		actionCalendar.setTime(actionDate);
		String fullDateStr = dateFormat.format(actionDate) + " " + timeFormat.format(actionDate);
		String dateStr = "<b>" + new DateRenderer().timeAgo(action.getDate()).formattedDate + "</b> ago";

		String dateDayStr = actionCalendar.get(Calendar.DAY_OF_MONTH) + "\n" + actionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());

		if (viewHolder.getDateDay() != null)
		{
			if (!lastDateStr.equalsIgnoreCase(dateDayStr))
			{
				lastDateStr = dateDayStr;
				viewHolder.getDateDay().setText(lastDateStr);
			}
			else
			{
				viewHolder.getDateDay().setText("");
			}
		}

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
		if (action.getClass() == Water.class)
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
				waterStr.append(ML.to(deliveryUnit, ((Water)action).getAmount()));
				waterStr.append(deliveryUnit.getLabel());
				waterStr.append(", ");
			}

			if (((Water)action).getTemp() != null)
			{
				waterStr.append("<b>Temp: </b>");
				waterStr.append(((Water)action).getTemp());
				waterStr.append("ºC, ");
			}

			summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) + "<br/>" : "";

			waterStr = new StringBuilder();

			if (((Water)action).getAdditives().size() > 0)
			{
				waterStr.append("<b>Additives:</b>");

				for (Additive additive : ((Water)action).getAdditives())
				{
					if (additive == null || additive.getAmount() == null) continue;

					double converted = ML.to(measureUnit, additive.getAmount());
					String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

					waterStr.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;• ");
					waterStr.append(additive.getDescription());
					waterStr.append("  -  ");
					waterStr.append(amountStr);
					waterStr.append(measureUnit.getLabel());
					waterStr.append("/");
					waterStr.append(deliveryUnit.getLabel());
				}
			}

			summary += waterStr.toString();
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
