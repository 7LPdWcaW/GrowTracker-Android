package me.anon.model;

import android.content.Context;

import me.anon.grow.R;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public enum PlantMedium
{
	SOIL(R.string.soil),
	HYDRO(R.string.hydroponics),
	COCO(R.string.coco_coir),
	AERO(R.string.aeroponics);

	private int printString;

	public int getPrintString()
	{
		return printString;
	}

	private PlantMedium(int name)
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
}
