package me.anon.lib.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.Plant;
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
			int currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

			return !preferences.contains("migration_" + currentVersion);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static void performMigration(Context context)
	{
		try
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			int currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;

			// reverse versions
			ArrayList<Integer> versions = new ArrayList<>();

			for (String key : preferences.getAll().keySet())
			{
				if (key.startsWith("migration_"))
				{
					versions.add(preferences.getInt(key, 0));
				}
			}

			Collections.sort(versions, new Comparator<Integer>()
			{
				@Override public int compare(Integer left, Integer right)
				{
					return left.compareTo(right);
				}
			});

			for (Integer version : versions)
			{
				// (* < 2.0) -> 2.0
				if (version < 6)
				{
					// migrate feedings to waterings
					//		-> nutrient object to additive
					for (Plant plant : PlantManager.getInstance().getPlants())
					{
						for (Action action : plant.getActions())
						{
							if (action instanceof Water && ((Water)action).getNutrient() != null)
							{
								Additive replacement = new Additive();
								replacement.setAmount(((Water)action).getMlpl());

								String nutrientStr = "";
								nutrientStr += ((Water)action).getNutrient().getNpc() == null ? "-" : ((Water)action).getNutrient().getNpc();
								nutrientStr += " : ";
								nutrientStr += ((Water)action).getNutrient().getPpc() == null ? "-" : ((Water)action).getNutrient().getPpc();
								nutrientStr += " : ";
								nutrientStr += ((Water)action).getNutrient().getKpc() == null ? "-" : ((Water)action).getNutrient().getKpc();

								if (((Water)action).getNutrient().getCapc() != null
								|| ((Water)action).getNutrient().getSpc() != null
								|| ((Water)action).getNutrient().getMgpc() != null)
								{
									nutrientStr += "/";
									nutrientStr += ((Water)action).getNutrient().getCapc() == null ? "-" : ((Water)action).getNutrient().getCapc();
									nutrientStr += " : ";
									nutrientStr += ((Water)action).getNutrient().getSpc() == null ? "-" : ((Water)action).getNutrient().getSpc();
									nutrientStr += " : ";
									nutrientStr += ((Water)action).getNutrient().getMgpc() == null ? "-" : ((Water)action).getNutrient().getMgpc();
								}

								replacement.setDescription(nutrientStr);
								((Water)action).getAdditives().add(replacement);
								((Water)action).setNutrient(null);
								((Water)action).setMlpl(null);
							}
						}
					}

					PlantManager.getInstance().save();
				}
			}

			preferences.edit().putInt("migration_" + currentVersion, currentVersion).apply();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
