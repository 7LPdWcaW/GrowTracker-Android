package me.anon.controller.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esotericsoftware.kryo.Kryo;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.lib.TempUnit;
import me.anon.lib.Unit;
import me.anon.lib.helper.TimeHelper;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.StageChange;
import me.anon.model.Water;
import me.anon.view.ActionHolder;
import me.anon.view.ImageActionHolder;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class ActionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter
{
	public interface OnActionSelectListener
	{
		public void onActionDeleted(Action action);
		public void onActionEdit(Action action);
		public void onActionCopy(Action action);
		public void onActionDuplicate(Action action);
	}

	private OnActionSelectListener onActionSelectListener;
	private Plant plant;
	private List<Action> actions = new ArrayList<>();
	private Unit measureUnit, deliveryUnit;
	private TempUnit tempUnit;
	private boolean usingEc = false;

	/**
	 * Dummy image action placeholder class
	 */
	private static class ImageAction extends Action
	{
		public ArrayList<String> images = new ArrayList<>();

		@Override public long getDate()
		{
			return getImageDate(images.get(0));
		}
	}

	public void setOnActionSelectListener(OnActionSelectListener onActionSelectListener)
	{
		this.onActionSelectListener = onActionSelectListener;
	}

	public Plant getPlant()
	{
		return plant;
	}

	public List<Action> getActions()
	{
		ArrayList<Action> actions = new ArrayList<>();
		for (Object item : this.actions)
		{
			if (item.getClass() != ImageAction.class) actions.add((Action)item);
		}

		return actions;
	}

	public Unit getMeasureUnit()
	{
		return measureUnit;
	}

	public Unit getDeliveryUnit()
	{
		return deliveryUnit;
	}

	public TempUnit getTempUnit()
	{
		return tempUnit;
	}

	public void setActions(Plant plant, List<Action> actions)
	{
		this.plant = plant;
		this.actions = new ArrayList<>();

		ArrayList<String> addedImages = new ArrayList<>();
		Collections.reverse(actions);
		for (Action item : actions)
		{
			ArrayList<String> groupedImages = new ArrayList<>();
			for (String image : plant.getImages())
			{
				long imageDate = getImageDate(image);

				if (imageDate <= item.getDate() && !addedImages.contains(image))
				{
					groupedImages.add(image);
					addedImages.add(image);
				}
			}

			if (!groupedImages.isEmpty())
			{
				Collections.sort(groupedImages, new Comparator<String>()
				{
					@Override public int compare(String o1, String o2)
					{
						long o1Date = getImageDate(o1);
						long o2Date = getImageDate(o2);

						if (o2Date < o1Date) return -1;
						if (o2Date > o1Date) return 1;
						return 0;
					}
				});
				ImageAction imageAction = new ImageAction();
				imageAction.images = groupedImages;
				this.actions.add(imageAction);
			}

			this.actions.add(item);
		}

		if (addedImages.size() != plant.getImages().size())
		{
			ArrayList<String> remainingImages = new ArrayList<>(plant.getImages());
			remainingImages.removeAll(addedImages);

			Collections.sort(remainingImages, new Comparator<String>()
			{
				@Override public int compare(String o1, String o2)
				{
					long o1Date = getImageDate(o1);
					long o2Date = getImageDate(o2);

					if (o2Date < o1Date) return -1;
					if (o2Date > o1Date) return 1;
					return 0;
				}
			});

			ImageAction imageAction = new ImageAction();
			imageAction.images = remainingImages;
			this.actions.add(imageAction);
		}

		Collections.reverse(this.actions);
	}

	private static long getImageDate(String image)
	{
		File currentImage = new File(image);
		long fileDate = Long.parseLong(currentImage.getName().replaceAll("[^0-9]", ""));

		if (fileDate == 0)
		{
			fileDate = currentImage.lastModified();
		}

		return fileDate;
	}

	@Override public int getItemViewType(int position)
	{
		if (actions.get(position).getClass() == ImageAction.class)
		{
			return 2;
		}

		return 1;
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
	{
		if (viewType == 2)
		{
			return new ImageActionHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.action_image, viewGroup, false));
		}

		return new ActionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.action_item, viewGroup, false));
	}

	@Override public void onBindViewHolder(final RecyclerView.ViewHolder vh, final int index)
	{
		final Action action = actions.get(index);
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(vh.itemView.getContext());
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(vh.itemView.getContext());
		TextView dateDay = null;
		TextView stageDay = null;

		if (vh instanceof ImageActionHolder)
		{
			final ImageActionHolder viewHolder = (ImageActionHolder)vh;
			viewHolder.bind(((ImageAction)action).images);
			dateDay = viewHolder.getDateDay();
			stageDay = viewHolder.getStageDay();
		}
		else if (vh instanceof ActionHolder)
		{
			final ActionHolder viewHolder = (ActionHolder)vh;

			if (measureUnit == null)
			{
				measureUnit = Unit.getSelectedMeasurementUnit(viewHolder.itemView.getContext());
			}

			if (deliveryUnit == null)
			{
				deliveryUnit = Unit.getSelectedDeliveryUnit(viewHolder.itemView.getContext());
			}

			if (tempUnit == null)
			{
				tempUnit = TempUnit.getSelectedTemperatureUnit(viewHolder.itemView.getContext());
			}

			usingEc = PreferenceManager.getDefaultSharedPreferences(viewHolder.itemView.getContext()).getBoolean("tds_ec", false);

			if (action == null) return;

			dateDay = viewHolder.getDateDay();
			stageDay = viewHolder.getStageDay();

			Date actionDate = new Date(action.getDate());
			Calendar actionCalendar = GregorianCalendar.getInstance();
			actionCalendar.setTime(actionDate);
			String fullDateStr = dateFormat.format(actionDate) + " " + timeFormat.format(actionDate);
			String dateStr = "<b>" + new DateRenderer().timeAgo(action.getDate()).formattedDate + "</b> ago";

			if (index > 0)
			{
				long difference = actions.get(index - 1).getDate() - action.getDate();
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
				summary += ((Water)action).getSummary(viewHolder.itemView.getContext());
				viewHolder.getCard().setCardBackgroundColor(0x9ABBDEFB);
				viewHolder.getName().setText("Watered");
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
									Kryo kryo = new Kryo();
									onActionSelectListener.onActionDuplicate(kryo.copy(action));
								}

								return true;
							}
							else if (item.getItemId() == R.id.copy)
							{
								if (onActionSelectListener != null)
								{
									Kryo kryo = new Kryo();
									onActionSelectListener.onActionCopy(kryo.copy(action));
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

		// plant date & stage
		Date actionDate = new Date(action.getDate());
		Calendar actionCalendar = GregorianCalendar.getInstance();
		actionCalendar.setTime(actionDate);

		String dateDayStr = actionCalendar.get(Calendar.DAY_OF_MONTH) + " " + actionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());

		if (dateDay != null)
		{
			String lastDateStr = "";

			if (index - 1 >= 0)
			{
				Date lastActionDate = new Date(actions.get(index - 1).getDate());
				Calendar lastActionCalendar = GregorianCalendar.getInstance();
				lastActionCalendar.setTime(lastActionDate);
				lastDateStr = lastActionCalendar.get(Calendar.DAY_OF_MONTH) + " " + lastActionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
			}

			if (!lastDateStr.equalsIgnoreCase(dateDayStr))
			{
				dateDay.setText(Html.fromHtml(dateDayStr));

				String stageDayStr = "";
				StageChange current = null;
				StageChange previous = null;

				for (int actionIndex = index; actionIndex < actions.size(); actionIndex++)
				{
					if (actions.get(actionIndex) instanceof StageChange)
					{
						if (current == null)
						{
							current = (StageChange)actions.get(actionIndex);
						}
						else if (previous == null)
						{
							previous = (StageChange)actions.get(actionIndex);
						}
					}
				}

				int totalDays = (int)TimeHelper.toDays(Math.abs(action.getDate() - plant.getPlantDate()));
				stageDayStr += totalDays;

				if (previous == null)
				{
					previous = current;
				}

				if (current != null)
				{
					if (action == current)
					{
						int currentDays = (int)TimeHelper.toDays(Math.abs(current.getDate() - previous.getDate()));
						stageDayStr += "/" + currentDays + previous.getNewStage().getPrintString().substring(0, 1).toLowerCase();
					}
					else
					{
						int currentDays = (int)TimeHelper.toDays(Math.abs(action.getDate() - current.getDate()));
						stageDayStr += "/" + currentDays + current.getNewStage().getPrintString().substring(0, 1).toLowerCase();
					}
				}

				stageDay.setText(stageDayStr);
			}
			else
			{
				dateDay.setText("");
				stageDay.setText("");
			}
		}
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
