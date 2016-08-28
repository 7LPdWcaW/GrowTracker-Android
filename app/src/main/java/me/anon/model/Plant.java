package me.anon.model;

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
	 * Stage is now calculated via latest {@link StageChange} action
	 */
	//@Deprecated private PlantStage stage;

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
