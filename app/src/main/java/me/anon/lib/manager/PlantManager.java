package me.anon.lib.manager;

import android.content.Context;

import com.google.gson.Gson;
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

	public void load()
	{
		String plantData = FileManager.getInstance().readFileAsString(FILES_DIR + "/plants.json");
		mPlants = (ArrayList<Plant>)new Gson().fromJson(plantData, new TypeToken<ArrayList<Plant>>(){}.getRawType());
	}
}
