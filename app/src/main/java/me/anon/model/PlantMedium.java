package me.anon.model;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public enum PlantMedium
{
	SOIL("Soil"),
	HYDRO("Hydroponics"),
	COCO("Coco Coir"),
	AERO("Aeroponics");

	private String printString;

	public String getPrintString()
	{
		return printString;
	}

	private PlantMedium(String name)
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
}
