package me.anon.controller.adapter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esotericsoftware.kryo.Kryo;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.jetbrains.annotations.NotNull;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import kotlinx.parcelize.Parcelize;
import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.lib.TempUnit;
import me.anon.lib.Unit;
import me.anon.lib.ext.IntUtilsKt;
import me.anon.lib.helper.TimeHelper;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantStage;
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

	public interface OnItemSelectCallback
	{
		public void onItemSelected(Action action);
	}

	private OnDateSelectedListener onDateSelectedListener;
	private OnItemSelectCallback onItemSelectCallback;
	private OnActionSelectListener onActionSelectListener;
	@Nullable private Plant plant;
	private List<Action> actions = new ArrayList<>();
	private Unit measureUnit, deliveryUnit;
	private TempUnit tempUnit;
	private boolean showDate = true;
	private boolean showActions = true;
	public boolean showCalendar = false;
	private CalendarDay selectedFilterDate = null;

	public void setFilterDate(CalendarDay selectedFilterDate)
	{
		this.selectedFilterDate = selectedFilterDate;
	}

	public void setOnDateChangedListener(OnDateSelectedListener onDateSelectedListener)
	{
		this.onDateSelectedListener = onDateSelectedListener;
	}

	/**
	 * Dummy image action placeholder class
	 */
	@SuppressLint("ParcelCreator") @Parcelize
	private static class ImageAction extends Action implements Parcelable
	{
		@NotNull @Override public String getTypeStr()
		{
			return "image";
		}

		public ArrayList<String> images = new ArrayList<>();

		@Override public long getDate()
		{
			if (images.size() <= 0) return 0;
			return getImageDate(images.get(0));
		}

		@Override public int describeContents()
		{
			return 0;
		}

		@Override public void writeToParcel(Parcel parcel, int i)
		{

		}
	}

	public void setOnActionSelectListener(OnActionSelectListener onActionSelectListener)
	{
		this.onActionSelectListener = onActionSelectListener;
	}

	public void setOnItemSelectCallback(OnItemSelectCallback onItemSelectCallback)
	{
		this.onItemSelectCallback = onItemSelectCallback;
	}

	public void setShowDate(boolean showDate)
	{
		this.showDate = showDate;
	}

	public void setShowActions(boolean showActions)
	{
		this.showActions = showActions;
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

	public void setActions(@Nullable Plant plant, ArrayList<Action> actions)
	{
		setActions(plant, actions, new ArrayList<Class>());
	}

	public void setActions(@Nullable Plant plant, ArrayList<Action> actions, ArrayList<Class> exclude)
	{
		this.plant = plant;
		this.actions = new ArrayList<>();

		ArrayList<String> addedImages = new ArrayList<>();
		Collections.reverse(actions);
		for (Action item : actions)
		{
			// force planted stage to use plant date
			if (item instanceof StageChange && ((StageChange)item).getNewStage() == PlantStage.PLANTED)
			{
				item.setDate(plant.getPlantDate());
			}

			if (plant != null)
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
			}

			this.actions.add(item);
		}

		if (plant != null && addedImages.size() != plant.getImages().size())
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
		if (showCalendar && position == 0)
		{
			return 3;
		}

		position = position - (showCalendar ? 1 : 0);

		if (selectedFilterDate != null)
		{
			LocalDate actionDate = CalendarDay.from(LocalDate.from(Instant.ofEpochMilli(actions.get(position).getDate()).atZone(ZoneId.systemDefault()))).getDate();
			if (!selectedFilterDate.getDate().equals(actionDate)) return 0;
		}

		if (actions.get(position).getClass() == ImageAction.class)
		{
			return 2;
		}

		return 1;
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
	{
		if (viewType == 3)
		{
			return new RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.calendar_item, viewGroup, false)){};
		}
		else if (viewType == 0)
		{
			return new RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty, viewGroup, false)){};
		}
		else if (viewType == 2)
		{
			return new ImageActionHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.action_image, viewGroup, false));
		}

		return new ActionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.action_item, viewGroup, false));
	}

	@Override public void onBindViewHolder(final RecyclerView.ViewHolder vh, final int index)
	{
		if (getItemViewType(index) == 0) return;

		if (getItemViewType(index) == 3)
		{
			final MaterialCalendarView calendar = (MaterialCalendarView)vh.itemView;
			calendar.removeDecorators();
			calendar.addDecorator(new DayViewDecorator()
			{
				@Override public boolean shouldDecorate(CalendarDay calendarDay)
				{
					// find an action that is on this day
					for (Action action : plant.getActions())
					{
						LocalDate actionDate = CalendarDay.from(LocalDate.from(Instant.ofEpochMilli(action.getDate()).atZone(ZoneId.systemDefault()))).getDate();
						if (calendarDay.getDate().equals(actionDate))
						{
							return true;
						}
					}

					return false;
				}

				@Override public void decorate(DayViewFacade dayViewFacade)
				{
					dayViewFacade.addSpan(new DotSpan(6.0f, IntUtilsKt.resolveColor(R.attr.colorAccent, calendar.getContext())));
				}
			});
			calendar.setOnDateChangedListener(onDateSelectedListener);
			calendar.setSelectedDate(selectedFilterDate);
			calendar.setCurrentDate(selectedFilterDate);

			return;
		}

		final int actionIndex = index - (showCalendar ? 1 : 0);
		final Action action = actions.get(actionIndex);
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

			if (action == null) return;

			dateDay = viewHolder.getDateDay();
			stageDay = viewHolder.getStageDay();

			Date actionDate = new Date(action.getDate());
			Calendar actionCalendar = GregorianCalendar.getInstance();
			actionCalendar.setTime(actionDate);
			String fullDateStr = dateFormat.format(actionDate) + " " + timeFormat.format(actionDate);
			String dateStr = vh.itemView.getContext().getString(R.string.ago, "<b>" + new DateRenderer(viewHolder.itemView.getContext()).timeAgo(action.getDate()).formattedDate + "</b>");

			if (actionIndex > 0)
			{
				long difference = actions.get(actionIndex - 1).getDate() - action.getDate();
				int days = (int)Math.round(((double)difference / 60d / 60d / 24d / 1000d));

				dateStr += " (-" + days + vh.itemView.getContext().getString(R.string.day_abbr) + ")";
			}

			viewHolder.getFullDate().setText(Html.fromHtml(fullDateStr));
			viewHolder.getDate().setText(Html.fromHtml(dateStr));
			viewHolder.getSummary().setVisibility(View.GONE);
			viewHolder.getCard().setCardBackgroundColor(IntUtilsKt.resolveColor(R.attr.colorSurface, viewHolder.itemView.getContext()));

			String summary = "";
			if (action.getClass() == Water.class)
			{
				summary += ((Water)action).getSummary(viewHolder.itemView.getContext());
				viewHolder.getCard().setCardBackgroundColor(viewHolder.itemView.getContext().getResources().getColor(R.color.light_blue));
				viewHolder.getName().setText(R.string.watered);
			}
			else if (action instanceof EmptyAction && ((EmptyAction)action).getAction() != null)
			{
				viewHolder.getName().setText(((EmptyAction)action).getAction().getPrintString());
				viewHolder.getCard().setCardBackgroundColor(((EmptyAction)action).getAction().getColour());
			}
			else if (action instanceof NoteAction)
			{
				viewHolder.getName().setText(R.string.note);
				viewHolder.getCard().setCardBackgroundColor(IntUtilsKt.getColor(R.color.light_grey, viewHolder.itemView.getContext()));
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

			if (showActions)
			{
				viewHolder.getOverflow().setVisibility(View.VISIBLE);
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
										.setTitle(R.string.delete_event_dialog_title)
										.setMessage(Html.fromHtml(v.getContext().getString(R.string.confirm_delete_item_message) + " <b>" + viewHolder.getName().getText() + "</b>?"))
										.setPositiveButton(R.string.confirm_positive, new DialogInterface.OnClickListener()
										{
											@Override public void onClick(DialogInterface dialog, int which)
											{
												if (onActionSelectListener != null)
												{
													onActionSelectListener.onActionDeleted(action);
												}
											}
										})
										.setNegativeButton(R.string.confirm_negative, null)
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
			else
			{
				viewHolder.getOverflow().setVisibility(View.GONE);
			}
		}

		if (showDate)
		{
			// plant date & stage
			Date actionDate = new Date(action.getDate());
			Calendar actionCalendar = GregorianCalendar.getInstance();
			actionCalendar.setTime(actionDate);

			String dateDayStr = actionCalendar.get(Calendar.DAY_OF_MONTH) + " " + actionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());

			if (dateDay != null)
			{
				String lastDateStr = "";

				if (actionIndex - 1 >= 0)
				{
					Date lastActionDate = new Date(actions.get(actionIndex - 1).getDate());
					Calendar lastActionCalendar = GregorianCalendar.getInstance();
					lastActionCalendar.setTime(lastActionDate);
					lastDateStr = lastActionCalendar.get(Calendar.DAY_OF_MONTH) + " " + lastActionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
				}

				if (!lastDateStr.equalsIgnoreCase(dateDayStr))
				{
					dateDay.setText(Html.fromHtml(dateDayStr));

					String stageDayStr = "";

					StageChange lastChange = null;
					long currentChangeDate = action.getDate();

					for (int aIndex = actionIndex; aIndex < actions.size(); aIndex++)
					{
						if (actions.get(aIndex) instanceof StageChange)
						{
							if (lastChange == null)
							{
								lastChange = (StageChange)actions.get(aIndex);
								break;
							}
						}
					}

					int totalDays = (int)TimeHelper.toDays(Math.abs(action.getDate() - plant.getPlantDate()));
					stageDayStr += (totalDays == 0 ? 1 : totalDays);

					if (lastChange != null)
					{
						int currentDays = (int)TimeHelper.toDays(Math.abs(currentChangeDate - lastChange.getDate()));
						currentDays = (currentDays == 0 ? 1 : currentDays);
						stageDayStr += "/" + currentDays + dateDay.getContext().getString(lastChange.getNewStage().getPrintString()).substring(0, 1).toLowerCase();
					}

					stageDay.setText(stageDayStr);
				}
				else
				{
					dateDay.setText("");
					stageDay.setText("");
				}

				((View)dateDay.getParent()).setVisibility(View.VISIBLE);
			}
		}
		else
		{
			if (dateDay != null && stageDay != null)
			{
				((View)dateDay.getParent()).setVisibility(View.GONE);
			}
		}

		if (onItemSelectCallback != null)
		{
			if (vh instanceof ActionHolder)
			{
				((ActionHolder)vh).getCard().setClickable(true);
				((ActionHolder)vh).getCard().setFocusable(true);

				((ActionHolder)vh).getCard().setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View v)
					{
						onItemSelectCallback.onItemSelected(action);
					}
				});
			}
		}
	}

	@Override public int getItemCount()
	{
		return actions.size() + (showCalendar ? 1 : 0);
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
