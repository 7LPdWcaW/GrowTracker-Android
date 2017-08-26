package me.anon.lib.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.anon.grow.MainApplication;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;

/**
 * Contains all methods and action types for available addons for Grow Tracker
 */
public class AddonHelper
{
	/**
	 * Array of broadcast actions
	 */
	public static final String[] ADDON_BROADCAST = {
		"me.anon.grow.ACTION_UPDATER",
		"me.anon.grow.ACTION_SAVE_PLANTS"
	};

	public static final Map<String, String> ADDON_DESCRIPTIONS = new HashMap<>();

	static
	{
		ADDON_DESCRIPTIONS.put(ADDON_BROADCAST[0], "Custom action for Grow Updater application. Triggered when app is opened.");
		ADDON_DESCRIPTIONS.put(ADDON_BROADCAST[1], "Listens for save events, i.e. when a plant is saved, or a photo taken/deleted.<br />Not triggered for garden edits.");
	}

	/**
	 * Array of startable activities
	 */
	public static final String[] ADDON_ACTIVITIES = {
		"me.anon.grow.ADDON_CONFIGURATION"
	};

	/**
	 * Sends `me.anon.grow.ACTION_SAVE_PLANTS` broadcast for plant list data
	 * @param context
	 */
	public static void broadcastPlantList(Context context)
	{
		final Context applicationContext = context.getApplicationContext();

		new Thread(new Runnable()
		{
			@Override public void run()
			{
				ArrayList<Plant> plants = new ArrayList<Plant>(PlantManager.getInstance().getPlants());
				String plantListData = GsonHelper.parse(plants);

				if (MainApplication.isEncrypted())
				{
					plantListData = Base64.encodeToString(EncryptionHelper.encrypt(MainApplication.getKey(), plantListData), Base64.NO_WRAP);
				}

				Intent saveRequest = new Intent("me.anon.grow.ACTION_SAVE_PLANTS");
				saveRequest.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				saveRequest.putExtra("me.anon.grow.PLANT_LIST", plantListData);
				saveRequest.putExtra("me.anon.grow.ENCRYPTED", MainApplication.isEncrypted());
				applicationContext.sendBroadcast(saveRequest);
			}
		}).start();
	}

	/**
	 * Sends `me.anon.grow.ACTION_SAVE_PLANTS` broadcast for image added/deleted
	 * @param context
	 * @param imagePath The path to the image data
	 * @param deleted True if the image was deleted, false if not
	 */
	public static void broadcastImage(Context context, String imagePath, boolean deleted)
	{
		Intent saveRequest = new Intent("me.anon.grow.ACTION_SAVE_PLANTS");
		saveRequest.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		saveRequest.putExtra(deleted ? "me.anon.grow.IMAGE_DELETED" : "me.anon.grow.IMAGE_ADDED", imagePath);
		saveRequest.putExtra("me.anon.grow.ENCRYPTED", MainApplication.isEncrypted());
		context.sendBroadcast(saveRequest);
	}
}
