package me.anon.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.anon.lib.DateRenderer;
import me.anon.lib.Unit;
import me.anon.lib.helper.TimeHelper;

import static me.anon.lib.Unit.ML;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Getter @Setter
@Accessors(prefix = {"m", ""}, chain = true)
public class Plant
{
	private String id = UUID.randomUUID().toString();
	private String name;
	private String strain;
	private long plantDate = System.currentTimeMillis();
	private boolean clone = false;
	private PlantMedium medium = PlantMedium.SOIL;
	private String mediumDetails;
	private ArrayList<String> images = new ArrayList<>();
	private ArrayList<Action> actions = new ArrayList<>();

	/**
	 * @return Gets the ID. If ID is null, a new ID will be generated for the model
	 */
	@NonNull
	public String getId()
	{
		if (id == null)
		{
			id = UUID.randomUUID().toString();
		}

		return id;
	}

	/**
	 * Stage is now calculated via latest {@link StageChange} action
	 */
	//@Deprecated private PlantStage stage;

	public String generateShortSummary(Context context)
	{
		Unit measureUnit = Unit.getSelectedMeasurementUnit(context);
		Unit deliveryUnit = Unit.getSelectedDeliveryUnit(context);

		String summary = "";

		if (getStage() == PlantStage.HARVESTED)
		{
			summary += "Harvested";
		}
		else
		{
			DateRenderer.TimeAgo planted = new DateRenderer().timeAgo(getPlantDate(), 3);
			summary += "<b>" + (int)planted.time + " " + planted.unit.type + "</b>";

			if (getActions() != null && getActions().size() > 0)
			{
				Water lastWater = null;

				ArrayList<Action> actions = getActions();
				for (int index = actions.size() - 1; index >= 0; index--)
				{
					Action action = actions.get(index);

					if (action.getClass() == Water.class && lastWater == null)
					{
						lastWater = (Water)action;
					}
				}

				SortedMap<PlantStage, Long> stageTimes = calculateStageTime();

				if (stageTimes.containsKey(getStage()))
				{
					summary += " / <b>" + (int)TimeHelper.toDays(stageTimes.get(getStage())) + getStage().getPrintString().substring(0, 1).toLowerCase() + "</b>";
				}

				if (lastWater != null)
				{
					summary += "<br/>";
					summary += "Watered: <b>" + new DateRenderer().timeAgo(lastWater.getDate()).formattedDate + "</b> ago";
					summary += "<br/>";

					if (lastWater.getPh() != null)
					{
						summary += "<b>" + lastWater.getPh() + " PH</b> ";

						if (lastWater.getRunoff() != null)
						{
							summary += "-> <b>" + lastWater.getRunoff() + " PH</b> ";
						}
					}

					if (lastWater.getAmount() != null)
					{
						summary += "<b>" + ML.to(deliveryUnit, lastWater.getAmount()) + deliveryUnit.getLabel() + "</b>";
					}
				}
			}
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length() - "<br/>".length());
		}

		return summary;
	}

	public String generateLongSummary(Context context)
	{
		Unit measureUnit = Unit.getSelectedMeasurementUnit(context);
		Unit deliveryUnit = Unit.getSelectedDeliveryUnit(context);

		String summary = "";
		summary += getStrain() + " - ";

		if (getStage() == PlantStage.HARVESTED)
		{
			summary += "Harvested";
		}
		else
		{
			DateRenderer.TimeAgo planted = new DateRenderer().timeAgo(getPlantDate(), 3);
			summary += "<b>Planted " + (int)planted.time + " " + planted.unit.type + " ago</b>";

			if (getActions() != null && getActions().size() > 0)
			{
				Water lastWater = null;

				ArrayList<Action> actions = getActions();
				for (int index = actions.size() - 1; index >= 0; index--)
				{
					Action action = actions.get(index);

					if (action.getClass() == Water.class && lastWater == null)
					{
						lastWater = (Water)action;
					}
				}

				SortedMap<PlantStage, Long> stageTimes = calculateStageTime();

				if (stageTimes.containsKey(getStage()))
				{
					summary += " / <b>" + (int)TimeHelper.toDays(stageTimes.get(getStage())) + getStage().getPrintString().substring(0, 1).toLowerCase() + "</b>";
				}

				if (lastWater != null)
				{
					summary += "<br/><br/>";
					summary += "Last watered: <b>" + new DateRenderer().timeAgo(lastWater.getDate()).formattedDate + "</b> ago";
					summary += "<br/>";

					if (lastWater.getPh() != null)
					{
						summary += "<b>" + lastWater.getPh() + " PH</b> ";

						if (lastWater.getRunoff() != null)
						{
							summary += "-> <b>" + lastWater.getRunoff() + " PH</b> ";
						}
					}

					if (lastWater.getAmount() != null)
					{
						summary += "<b>" + ML.to(deliveryUnit, lastWater.getAmount()) + deliveryUnit.getLabel() + "</b>";
					}

					if (lastWater.getAdditives().size() > 0)
					{
						double total = 0d;
						for (Additive additive : lastWater.getAdditives())
						{
							total += additive.getAmount();
						}

						summary += "<br/> + <b>" + ML.to(measureUnit, total) + measureUnit.getLabel() + "</b> additives";
					}
				}
			}
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length() - "<br/>".length());
		}

		return summary;
	}

	@Nullable
	public PlantStage getStage()
	{
		for (int index = actions.size() - 1; index >= 0; index--)
		{
			if (actions.get(index) instanceof StageChange)
			{
				return ((StageChange)actions.get(index)).getNewStage();
			}
		}

		// This should never be reached.
		return null;
	}

	/**
	 * Returns a map of plant stages
	 * @return
	 */
	public Map<PlantStage, Action> getStages()
	{
		HashMap<PlantStage, Action> stages = new HashMap<>();

		for (int index = actions.size() - 1; index >= 0; index--)
		{
			if (actions.get(index) instanceof StageChange)
			{
				stages.put(((StageChange)actions.get(index)).getNewStage(), actions.get(index));
			}
		}

		return stages;
	}

	/**
	 * Calculates the time spent in each plant stage
	 *
	 * @return The list of plant stages with time in milliseconds. Keys are in order of stage defined in {@link PlantStage}
	 */
	public SortedMap<PlantStage, Long> calculateStageTime()
	{
		long startDate = getPlantDate();
		long endDate = System.currentTimeMillis();
		SortedMap<PlantStage, Long> stages = new TreeMap<PlantStage, Long>(new Comparator<PlantStage>()
		{
			@Override public int compare(PlantStage lhs, PlantStage rhs)
			{
				if (lhs.ordinal() < rhs.ordinal())
				{
					return 1;
				}
				else if (lhs.ordinal() > rhs.ordinal())
				{
					return -1;
				}

				return 0;
			}
		});

		for (Action action : getActions())
		{
			if (action instanceof StageChange)
			{
				stages.put(((StageChange)action).getNewStage(), action.getDate());

				if (((StageChange)action).getNewStage() == PlantStage.HARVESTED)
				{
					endDate = action.getDate();
				}
			}
		}

		int stageIndex = 0;
		long lastStage = 0;
		PlantStage previous = stages.firstKey();
		for (PlantStage plantStage : stages.keySet())
		{
			long difference = 0;
			if (stageIndex == 0)
			{
				difference = endDate - stages.get(plantStage);
			}
			else
			{
				difference = lastStage - stages.get(plantStage);
			}

			previous = plantStage;
			lastStage = stages.get(plantStage);
			stageIndex++;

			stages.put(plantStage, difference);
		}

		return stages;
	}
}
