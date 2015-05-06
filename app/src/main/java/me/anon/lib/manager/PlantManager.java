package me.anon.lib.manager;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public class PlantManager
{
	@Getter(lazy = true) private static final PlantManager instance = new PlantManager();

	private static String FILES_DIR;

	private ArrayList<Plant> mPlants;
	private Context context;

	private PlantManager(){}

	public void initialise(Context context)
	{
		this.context = context.getApplicationContext();
		FILES_DIR = this.context.getFilesDir().getAbsolutePath();

		load();
	}

	public void addPlant(Plant plant)
	{
		mPlants.add(plant);
		save();
	}

	public void load()
	{
		if (FileManager.getInstance().fileExists(FILES_DIR + "/plants.json"))
		{
			String plantData = FileManager.getInstance().readFileAsString(FILES_DIR + "/plants.json");

			try
			{
				if (!TextUtils.isEmpty(plantData))
				{
					mPlants = (ArrayList<Plant>)new Gson().fromJson(plantData, new TypeToken<ArrayList<Plant>>(){}.getRawType());
				}
			}
			catch (JsonSyntaxException e)
			{
				e.printStackTrace();
			}
		}

		if (mPlants == null)
		{
			mPlants = new ArrayList<>();
		}
	}

	public void save()
	{
		FileManager.getInstance().writeFile(FILES_DIR + "/plants.json", new Gson().toJson(mPlants));
	}
}
