package me.anon.lib.manager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;

import me.anon.grow.MainApplication;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.GsonHelper;
import me.anon.model.Garden;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class GardenManager
{
	private static final GardenManager instance = new GardenManager();

	public static GardenManager getInstance()
	{
		return instance;
	}

	public static String FILES_DIR;

	private final ArrayList<Garden> mGardens = new ArrayList<>();
	private Context context;

	private GardenManager(){}

	public ArrayList<Garden> getGardens()
	{
		return mGardens;
	}

	public void initialise(Context context)
	{
		this.context = context.getApplicationContext();
		FILES_DIR = this.context.getExternalFilesDir(null).getAbsolutePath();

		load();
	}

	public void load()
	{
		if (FileManager.getInstance().fileExists(FILES_DIR + "/gardens.json"))
		{
			String plantData;

			if (MainApplication.isEncrypted())
			{
				if (TextUtils.isEmpty(MainApplication.getKey()))
				{
					return;
				}

				plantData = EncryptionHelper.decrypt(MainApplication.getKey(), FileManager.getInstance().readFile(FILES_DIR + "/gardens.json"));
			}
			else
			{
				plantData = FileManager.getInstance().readFileAsString(FILES_DIR + "/gardens.json");
			}

			try
			{
				if (!TextUtils.isEmpty(plantData))
				{
					mGardens.clear();
					mGardens.addAll((ArrayList<Garden>)GsonHelper.parse(plantData, new TypeToken<ArrayList<Garden>>(){}.getType()));
				}
			}
			catch (JsonSyntaxException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void save()
	{
		synchronized (mGardens)
		{
			if (MainApplication.isEncrypted())
			{
				if (TextUtils.isEmpty(MainApplication.getKey()))
				{
					return;
				}

				FileManager.getInstance().writeFile(FILES_DIR + "/gardens.json", EncryptionHelper.encrypt(MainApplication.getKey(), GsonHelper.parse(mGardens)));
			}
			else
			{
				FileManager.getInstance().writeFile(FILES_DIR + "/gardens.json", GsonHelper.parse(mGardens));
			}

			if (new File(FILES_DIR + "/gardens.json").length() == 0 || !new File(FILES_DIR + "/gardens.json").exists())
			{
				Toast.makeText(context, "There was a fatal problem saving the garden data, please backup this data", Toast.LENGTH_LONG).show();
				String sendData = GsonHelper.parse(mGardens);
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, "== WARNING : PLEASE BACK UP THIS DATA == \r\n\r\n " + sendData);
				context.startActivity(share);
			}
		}
	}

	public void insert(Garden garden)
	{
		mGardens.add(garden);
		save();
	}
}
