package me.anon.lib.manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.anon.grow.MainApplication;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.GsonHelper;
import me.anon.lib.task.AsyncCallback;
import me.anon.model.Garden;
import me.anon.model.Plant;
import me.anon.model.PlantStage;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public class PlantManager
{
	@Getter(lazy = true) private static final PlantManager instance = new PlantManager();

	private static String FILES_DIR;

	private final ArrayList<Plant> mPlants = new ArrayList<>();
	private Context context;

	private PlantManager(){}

	public void initialise(Context context)
	{
		this.context = context.getApplicationContext();
		FILES_DIR = this.context.getExternalFilesDir(null).getAbsolutePath();

		load();
	}

	public ArrayList<Plant> getSortedPlantList(@Nullable Garden garden)
	{
		synchronized (this.mPlants)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			int plantsSize =  PlantManager.getInstance().getPlants().size();
			Plant[] ordered = new Plant[garden == null ? plantsSize : garden.getPlantIds().size()];

			boolean hideHarvested = prefs.getBoolean("hide_harvested", false);

			for (int index = 0; index < plantsSize; index++)
			{
				Plant plant = PlantManager.getInstance().getPlants().get(index);

				if (plant == null || (hideHarvested && plant.getStage() == PlantStage.HARVESTED) || (garden != null && !garden.getPlantIds().contains(plant.getId())))
				{
					continue;
				}

				ordered[garden == null ? index : garden.getPlantIds().indexOf(plant.getId())] = plant;
			}

			ArrayList<Plant> finalList = new ArrayList<>();
			finalList.addAll(Arrays.asList(ordered));
			finalList.removeAll(Collections.singleton(null));

			return finalList;
		}
	}

	public void setPlants(ArrayList<Plant> plants)
	{
		synchronized (this.mPlants)
		{
			this.mPlants.clear();
			this.mPlants.addAll(plants);
		}
	}

	public void addPlant(Plant plant)
	{
		synchronized (this.mPlants)
		{
			mPlants.add(plant);
			save();
		}
	}

	public void deletePlant(int plantIndex)
	{
		synchronized (this.mPlants)
		{
			// Delete images
			ArrayList<String> imagePaths = mPlants.get(plantIndex).getImages();
			for (String filePath : imagePaths)
			{
				new File(filePath).delete();
			}

			// Remove plant
			mPlants.remove(plantIndex);

			// Remove from shared prefs
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			prefs.edit().remove(String.valueOf(plantIndex)).apply();
		}
	}

	public void upsert(int index, Plant plant)
	{
		if (index < 0)
		{
			addPlant(plant);
		}
		else
		{
			mPlants.set(index, plant);
			save();
		}
	}

	public void load()
	{
		if (FileManager.getInstance().fileExists(FILES_DIR + "/plants.json"))
		{
			String plantData;

			if (MainApplication.isEncrypted())
			{
				if (TextUtils.isEmpty(MainApplication.getKey()))
				{
					return;
				}

				plantData = EncryptionHelper.decrypt(MainApplication.getKey(), FileManager.getInstance().readFile(FILES_DIR + "/plants.json"));
			}
			else
			{
				plantData = FileManager.getInstance().readFileAsString(FILES_DIR + "/plants.json");
			}

			try
			{
				if (!TextUtils.isEmpty(plantData))
				{
					mPlants.clear();
					mPlants.addAll((ArrayList<Plant>)GsonHelper.parse(plantData, new TypeToken<ArrayList<Plant>>(){}.getType()));
				}
			}
			catch (JsonSyntaxException e)
			{
				e.printStackTrace();
			}
		}
	}

	public synchronized void save()
	{
		synchronized (this.mPlants)
		{
			save(null);
		}
	}

	public void save(final AsyncCallback callback)
	{
		synchronized (mPlants)
		{
			new AsyncTask<Void, Void, Void>()
			{
				@Override protected Void doInBackground(Void... voids)
				{
					synchronized (mPlants)
					{
						if (MainApplication.isEncrypted())
						{
							if (TextUtils.isEmpty(MainApplication.getKey()))
							{
								return null;
							}

							FileManager.getInstance().writeFile(FILES_DIR + "/plants.json", EncryptionHelper.encrypt(MainApplication.getKey(), GsonHelper.parse(mPlants)));
						}
						else
						{
							FileManager.getInstance().writeFile(FILES_DIR + "/plants.json", GsonHelper.parse(mPlants));
						}
					}

					return null;
				}

				@Override protected void onPostExecute(Void aVoid)
				{
					if (callback != null)
					{
						callback.callback();
					}

					if (new File(FILES_DIR + "/plants.json").length() == 0 || !new File(FILES_DIR + "/plants.json").exists())
					{
						Toast.makeText(context, "There was a fatal problem saving the plant data, please backup this data", Toast.LENGTH_LONG).show();
						String sendData = GsonHelper.parse(mPlants);
						Intent share = new Intent(Intent.ACTION_SEND);
						share.setType("text/plain");
						share.putExtra(Intent.EXTRA_TEXT, "== WARNING : PLEASE BACK UP THIS DATA == \r\n\r\n " + sendData);
						context.startActivity(share);
					}
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}
}
