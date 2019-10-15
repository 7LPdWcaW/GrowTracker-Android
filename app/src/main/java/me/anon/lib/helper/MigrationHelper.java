package me.anon.lib.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import me.anon.lib.TdsUnit;
import me.anon.lib.Unit;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.AsyncCallback;
import me.anon.model.Action;
import me.anon.model.Plant;
import me.anon.model.Tds;
import me.anon.model.Water;

/**
 * Helps with migrating from old versions of the app
 */
public class MigrationHelper
{
	public static boolean needsMigration(Context context)
	{
		try
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			return !preferences.contains("migration_tds");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static boolean performMigration(Context context, final AsyncCallback callback)
	{
		try
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			if (!preferences.getBoolean("migration_tds", false))
			{
				// migrate ppm to tds
				boolean usingEc = preferences.getBoolean("tds_ec", false);
				for (Plant plant : PlantManager.getInstance().getPlants())
				{
					for (Action action : plant.getActions())
					{
						if (action instanceof Water && ((Water)action).getPpm() != null)
						{
							Tds replacement = new Tds();
							if (usingEc && ((Water)action).getPpm() < 25)
							{
								replacement.setAmount(Unit.toTwoDecimalPlaces((((Water)action).getPpm() * 2d) / 1000d));
								replacement.setType(TdsUnit.EC);
							}
							else
							{
								replacement.setAmount(((Water)action).getPpm());
								replacement.setType(TdsUnit.PPM500);
							}

							((Water)action).setTds(replacement);
							((Water)action).setPpm(null);
						}
					}
				}

				preferences.edit().putBoolean("migration_tds", true).apply();
				PlantManager.getInstance().save(callback);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
