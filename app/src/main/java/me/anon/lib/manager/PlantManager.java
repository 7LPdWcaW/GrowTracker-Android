package me.anon.lib.manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Types;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import me.anon.grow.BootActivity;
import me.anon.grow.MainApplication;
import me.anon.lib.helper.AddonHelper;
import me.anon.lib.helper.BackupHelper;
import me.anon.lib.helper.MoshiHelper;
import me.anon.lib.stream.DecryptInputStream;
import me.anon.lib.stream.EncryptOutputStream;
import me.anon.lib.task.AsyncCallback;
import me.anon.model.Garden;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class PlantManager
{
	private static final PlantManager instance = new PlantManager();

	public static PlantManager getInstance()
	{
		return instance;
	}

	public static String FILES_DIR;

	private final ArrayList<Plant> mPlants = new ArrayList<>();
	private Context context;

	private PlantManager(){}

	public ArrayList<Plant> getPlants()
	{
		return mPlants;
	}

	public void initialise(Context context)
	{
		this.context = context.getApplicationContext();
		FILES_DIR = this.context.getExternalFilesDir(null).getAbsolutePath();

		load();
	}

	public ArrayList<Plant> getSortedPlantList(@Nullable Garden garden)
	{
		if (MainApplication.isFailsafe())
		{
			return new ArrayList<>();
		}

		synchronized (this.mPlants)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			int plantsSize =  PlantManager.getInstance().getPlants().size();
			Plant[] ordered = new Plant[garden == null ? plantsSize : garden.getPlantIds().size()];

			for (int index = 0; index < plantsSize; index++)
			{
				Plant plant = PlantManager.getInstance().getPlants().get(index);

				if (plant == null || (garden != null && !garden.getPlantIds().contains(plant.getId())))
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
			if (!MainApplication.isFailsafe())
			{
				this.mPlants.clear();
				this.mPlants.addAll(plants);
			}
		}
	}

	public void addPlant(Plant plant)
	{
		synchronized (this.mPlants)
		{
			if (!MainApplication.isFailsafe())
			{
				mPlants.add(plant);
				save();
			}
		}
	}

	public void deletePlant(final int plantIndex, final AsyncCallback callback)
	{
//		synchronized (this.mPlants)
		{
			if (MainApplication.isFailsafe()) return;

			new AsyncTask<Void, Void, Void>()
			{
				@Override protected Void doInBackground(Void... params)
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

					return null;
				}

				@Override protected void onPostExecute(Void aVoid)
				{
					if (callback != null)
					{
						callback.callback();
					}
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

	public boolean load()
	{
		return load(false);
	}

	public boolean load(boolean fromRestore)
	{
		if (MainApplication.isFailsafe())
		{
			return false;
		}

		// redundancy check
		if (new File(FILES_DIR, "/plants.json").lastModified() < new File(FILES_DIR, "/plants.json.bak").lastModified())
		{
			FileManager.getInstance().copyFile(FILES_DIR + "/plants.json.bak", FILES_DIR + "/plants.json");
		}

		if (FileManager.getInstance().fileExists(FILES_DIR + "/plants.json"))
		{
			String plantData;

			try
			{
				if (MainApplication.isEncrypted())
				{
					if (TextUtils.isEmpty(MainApplication.getKey()))
					{
						return false;
					}

					DecryptInputStream stream = new DecryptInputStream(MainApplication.getKey(), new File(FILES_DIR, "/plants.json"));

					mPlants.clear();
					mPlants.addAll(MoshiHelper.parse(stream, Types.newParameterizedType(ArrayList.class, Plant.class)));
					MainApplication.setFailsafe(false);
				}
				else
				{
					mPlants.clear();
					mPlants.addAll(MoshiHelper.parse(new FileInputStream(new File(FILES_DIR, "/plants.json")), Types.newParameterizedType(ArrayList.class, Plant.class)));
					MainApplication.setFailsafe(false);
				}

				return true;
			}
			catch (final JsonDataException e)
			{
				e.printStackTrace();

				if (!fromRestore)
				{
					File backupPath = BackupHelper.backupJson();
					Toast.makeText(context, "There is a syntax error in your app data. Your data has been backed up to " + backupPath.getPath() + ". Please fix before re-opening the app.\n" + e.getMessage(), Toast.LENGTH_LONG).show();

					// prevent save
					MainApplication.setFailsafe(true);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();

				if (!fromRestore)
				{
					File backupPath = BackupHelper.backupJson();
					Toast.makeText(context, "There is a problem loading your app data. Your data has been backed up to " + backupPath.getPath(), Toast.LENGTH_LONG).show();

					// prevent save
					MainApplication.setFailsafe(true);
				}
			}
		}

		return false;
	}

	public void save()
	{
		if (!MainApplication.isFailsafe())
		{
			if (mPlants.size() > 0)
			{
				save(null);
			}
		}
	}

	public void save(final AsyncCallback callback)
	{
		save(callback, false);
	}

	private Queue<SaveAsyncTask> saveTask = new ConcurrentLinkedQueue<>();
	private AtomicBoolean isSaving = new AtomicBoolean(false);

	public void save(final AsyncCallback callback, boolean ignoreCheck)
	{
		synchronized (mPlants)
		{
			if (MainApplication.isFailsafe()) return;

			if ((!ignoreCheck && mPlants.size() > 0) || ignoreCheck)
			{
				saveTask.add(new SaveAsyncTask(mPlants)
				{
					@Override protected Void doInBackground(Void... params)
					{
						FileManager.getInstance().copyFile(FILES_DIR + "/plants.json", FILES_DIR + "/plants.json.bak");

						try
						{
							OutputStream outstream = null;

							if (MainApplication.isEncrypted())
							{
								if (TextUtils.isEmpty(MainApplication.getKey()))
								{
									return null;
								}

								outstream = new EncryptOutputStream(MainApplication.getKey(), new File(FILES_DIR + "/plants.json"));
							}
							else
							{
								outstream = new FileOutputStream(new File(FILES_DIR + "/plants.json"));
							}

							MoshiHelper.toJson(plants, Types.newParameterizedType(ArrayList.class, Plant.class), new BufferedOutputStream(outstream, 8192));
						}
						catch (Exception e)
						{
							e.printStackTrace();
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
							String sendData = MoshiHelper.toJson(mPlants, Types.newParameterizedType(ArrayList.class, Plant.class));
							Intent share = new Intent(Intent.ACTION_SEND);
							share.setType("text/plain");
							share.putExtra(Intent.EXTRA_TEXT, "== WARNING : PLEASE BACK UP THIS DATA == \r\n\r\n " + sendData);
							context.startActivity(share);
						}

						if (saveTask.size() > 0)
						{
							saveTask.poll().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
						else
						{
							isSaving.set(false);
						}

						AddonHelper.broadcastPlantList(context);
					}
				});

				if (!isSaving.get())
				{
					isSaving.set(true);
					saveTask.poll().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
			else
			{
				load();
				Intent restart = new Intent(context, BootActivity.class);
				restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(restart);
			}
		}
	}

	public abstract static class SaveAsyncTask extends AsyncTask<Void, Void, Void>
	{
		protected List<Plant> plants;

		public SaveAsyncTask(List<Plant> plants)
		{
			this.plants = new ArrayList(plants);
		}
	}
}
