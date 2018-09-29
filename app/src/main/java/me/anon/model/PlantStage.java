package me.anon.model;

import android.support.annotation.Nullable;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public enum PlantStage
{
	PLANTED("Planted"),
	GERMINATION("Germination"),
	CUTTING("Cutting"),
	VEGETATION("Vegetation"),
	FLOWER("Flower"),
	DRYING("Drying"),
	CURING("Curing"),
	HARVESTED("Harvested");

	private String printString;

	public String getPrintString()
	{
		return printString;
	}

	private PlantStage(String name)
	{
		this.printString = name;
	}

	public static String[] names()
	{
		String[] names = new String[values().length];
		for (int index = 0; index < names.length; index++)
		{
			names[index] = values()[index].getPrintString();
		}

		return names;
	}

	@Nullable
	public static PlantStage valueOfPrintString(String printString)
	{
		for (PlantStage plantStage : values())
		{
			if (plantStage.printString.equals(printString))
			{
				return plantStage;
			}
		}

		return null;
	}
}
