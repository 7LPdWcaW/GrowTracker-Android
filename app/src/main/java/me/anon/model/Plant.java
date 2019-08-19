package me.anon.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.anon.grow.R;
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
public class Plant
{
	private String id = UUID.randomUUID().toString();
	private String name;
	private String strain = null;
	private long plantDate = System.currentTimeMillis();
	private boolean clone = false;
	private PlantMedium medium = PlantMedium.SOIL;
	private String mediumDetails = null;
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

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStrain()
	{
		return strain;
	}

	public void setStrain(String strain)
	{
		this.strain = strain;
	}

	public long getPlantDate()
	{
		return plantDate;
	}

	public void setPlantDate(long plantDate)
	{
		this.plantDate = plantDate;
	}

	public boolean isClone()
	{
		return clone;
	}

	public void setClone(boolean clone)
	{
		this.clone = clone;
	}

	public PlantMedium getMedium()
	{
		return medium;
	}

	public void setMedium(PlantMedium medium)
	{
		this.medium = medium;
	}

	public String getMediumDetails()
	{
		return mediumDetails;
	}

	public void setMediumDetails(String mediumDetails)
	{
		this.mediumDetails = mediumDetails;
	}

	public ArrayList<String> getImages()
	{
		return images;
	}

	public void setImages(ArrayList<String> images)
	{
		this.images = images;
	}

	public ArrayList<Action> getActions()
	{
		return actions;
	}

	public void setActions(ArrayList<Action> actions)
	{
		this.actions = actions;
	}

	/**
	 * Stage is now calculated via latest {@link StageChange} action
	 */
	//@Deprecated private PlantStage stage;


}
