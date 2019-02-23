package me.anon.model;

import java.util.ArrayList;

public class Garden
{
	protected String name;
	protected ArrayList<String> plantIds;
	protected GardenType type = GardenType.GARDEN;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ArrayList<String> getPlantIds()
	{
		return plantIds;
	}

	public void setPlantIds(ArrayList<String> plantIds)
	{
		this.plantIds = plantIds;
	}

	public GardenType getType()
	{
		return type;
	}

	public void setType(GardenType type)
	{
		this.type = type;
	}

	public static enum GardenType
	{
		GARDEN,
		LINKED_PLANTS
	}
}
