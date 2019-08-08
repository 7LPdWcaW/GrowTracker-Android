package me.anon.model;

import android.content.Context;
import android.support.annotation.Nullable;

import me.anon.grow.R;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public enum PlantStage
{
	PLANTED(R.string.planted),
	GERMINATION(R.string.germination),
	SEEDLING(R.string.seedling),
	CUTTING(R.string.cutting),
	VEGETATION(R.string.vegetation),
	FLOWER(R.string.flowering),
	DRYING(R.string.drying),
	CURING(R.string.curing),
	HARVESTED(R.string.harvested);

	private int printString;

	public int getPrintString()
	{
		return printString;
	}

	private PlantStage(int name)
	{
		this.printString = name;
	}

	public static String[] names(Context context)
	{
		String[] names = new String[values().length];
		for (int index = 0; index < names.length; index++)
		{
			names[index] = context.getString(values()[index].getPrintString());
		}

		return names;
	}

	@Nullable
	public static PlantStage valueOfPrintString(Context context, String printString)
	{
		for (PlantStage plantStage : values())
		{
			if (context.getString(plantStage.printString).equals(printString))
			{
				return plantStage;
			}
		}

		return null;
	}
}
