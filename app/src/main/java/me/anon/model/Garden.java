package me.anon.model;

import java.util.ArrayList;

public class Garden
{
	protected String name;
	protected ArrayList<String> plantIds;

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
}
