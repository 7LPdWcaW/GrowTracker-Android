package me.anon.lib.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.SortedMap;

import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Feed;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;

/**
 * Helper class for exporting plant data into a Tarball file
 */
public class ExportHelper
{
	private static final String NEW_LINE = "\r\n\r\n";

	/**
	 * @param plant
	 * @return
	 */
	@Nullable public static File exportPlant(Context context, @NonNull Plant plant)
	{
		long startDate = plant.getPlantDate();
		long endDate = System.currentTimeMillis();
		long feedDifference = 0L;
		long waterDifference = 0L;
		long lastFeed = 0L, lastWater = 0L;
		int totalFeed = 0, totalWater = 0, totalFlush = 0;

		for (Action action : plant.getActions())
		{
			if (action instanceof StageChange)
			{
				if (((StageChange)action).getNewStage() == PlantStage.HARVESTED)
				{
					endDate = action.getDate();
				}
			}

			if (action instanceof Feed)
			{
				if (lastFeed != 0)
				{
					feedDifference += Math.abs(action.getDate() - lastFeed);
				}

				totalFeed++;
				lastFeed = action.getDate();

			}
			else if (action instanceof Water)
			{
				if (lastWater != 0)
				{
					waterDifference += Math.abs(action.getDate() - lastWater);
				}

				totalWater++;
				lastWater = action.getDate();
			}
			else if (action instanceof EmptyAction && ((EmptyAction)action).getAction() == Action.ActionName.FLUSH)
			{
				totalFlush++;
			}
		}

		long seconds = ((endDate - startDate) / 1000);
		double days = (double)seconds * 0.0000115741d;

		StringBuffer plantDetails = new StringBuffer(1000);
		plantDetails.append("#").append(plant.getName()).append(" Grow Log");
		plantDetails.append(NEW_LINE);
		plantDetails.append("*Strain*: ").append(plant.getStrain());
		plantDetails.append(NEW_LINE);
		plantDetails.append("*Is clone?*: ").append(plant.isClone());
		plantDetails.append(NEW_LINE);
		plantDetails.append("*Medium*: ").append(plant.getMedium().getPrintString());
		plantDetails.append(NEW_LINE);

		plantDetails.append("##Stages");
		plantDetails.append(NEW_LINE);

		SortedMap<PlantStage, Long> stages = plant.calculateStageTime();
		for (PlantStage plantStage : stages.keySet())
		{
			plantDetails.append("- *").append(plantStage.getPrintString()).append("*: ");
			plantDetails.append(printableDate(context, plant.getPlantDate()));

			if (plantStage != PlantStage.PLANTED && plantStage != PlantStage.HARVESTED)
			{
				plantDetails.append(" (").append((int)TimeHelper.toDays(stages.get(plantStage))).append(" days)");
			}

			plantDetails.append(NEW_LINE);
		}

		plantDetails.append("##General stats");
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Total grow time*: ").append(String.format("%1$,.2f days", days));
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Total feeds*: ").append(String.valueOf(totalFeed));
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Total waters*: ").append(String.valueOf(totalWater));
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Total flushes*: ").append(String.valueOf(totalFlush));
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Average time between feeds*: ").append(String.format("%1$,.2f days", (TimeHelper.toDays(feedDifference) / (double)totalFeed)));
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Average time between waterings*: ").append(String.format("%1$,.2f days", (TimeHelper.toDays(waterDifference) / (double)totalWater)));
		plantDetails.append(NEW_LINE);

		plantDetails.append("##Timeline");
		plantDetails.append(NEW_LINE);

		for (Action action : plant.getActions())
		{
			plantDetails.append("###").append(printableDate(context, action.getDate()));
			plantDetails.append(NEW_LINE);

			if (action.getClass() == Feed.class)
			{
				plantDetails.append("*Type*: Feeding");
				plantDetails.append(NEW_LINE);

				if (((Feed)action).getNutrient() != null)
				{
					plantDetails.append("Fed with ");

					if (((Feed)action).getMlpl() != null)
					{
						plantDetails.append(((Feed)action).getMlpl()).append(" ml/l of ");
					}

					plantDetails.append(((Feed)action).getNutrient().getNpc() == null ? "-" : ((Feed)action).getNutrient().getNpc());
					plantDetails.append(":");
					plantDetails.append(((Feed)action).getNutrient().getPpc() == null ? "-" : ((Feed)action).getNutrient().getPpc());
					plantDetails.append(":");
					plantDetails.append(((Feed)action).getNutrient().getKpc() == null ? "-" : ((Feed)action).getNutrient().getKpc());

					if (((Feed)action).getNutrient().getMgpc() != null
					|| ((Feed)action).getNutrient().getSpc() != null
					|| ((Feed)action).getNutrient().getCapc() != null)
					{
						plantDetails.append(" / ");
						plantDetails.append(((Feed)action).getNutrient().getCapc() == null ? "-" : ((Feed)action).getNutrient().getCapc());
						plantDetails.append(":");
						plantDetails.append(((Feed)action).getNutrient().getSpc() == null ? "-" : ((Feed)action).getNutrient().getSpc());
						plantDetails.append(":");
						plantDetails.append(((Feed)action).getNutrient().getMgpc() == null ? "-" : ((Feed)action).getNutrient().getMgpc());
					}

					plantDetails.append(NEW_LINE);
				}
			}
			else if (action.getClass() == Water.class)
			{
				plantDetails.append("*Type*: Watering");
				plantDetails.append(NEW_LINE);
			}
			else if (action instanceof EmptyAction && ((EmptyAction)action).getAction() != null)
			{
				plantDetails.append(((EmptyAction)action).getAction().getPrintString());
				plantDetails.append(NEW_LINE);
			}
			else if (action instanceof NoteAction)
			{
				plantDetails.append("*Note*");
				plantDetails.append(NEW_LINE);
			}
			else if (action instanceof StageChange)
			{
				plantDetails.append("*Changed state to*: ").append(((StageChange)action).getNewStage().getPrintString());
				plantDetails.append(NEW_LINE);
			}

			if (Water.class.isAssignableFrom(action.getClass()))
			{
				boolean newLine = false;

				if (((Water)action).getPh() != null)
				{
					plantDetails.append("*In pH*: ");
					plantDetails.append(((Water)action).getPh());
					plantDetails.append(", ");
					newLine = true;
				}

				if (((Water)action).getRunoff() != null)
				{
					plantDetails.append("*Out pH*: ");
					plantDetails.append(((Water)action).getRunoff());
					plantDetails.append(", ");
					newLine = true;
				}

				if (((Water)action).getPpm() != null)
				{
					plantDetails.append("*PPM*: ");
					plantDetails.append(((Water)action).getPpm());
					plantDetails.append(", ");
					newLine = true;
				}

				if (((Water)action).getAmount() != null)
				{
					plantDetails.append("*Amount*: ");
					plantDetails.append(((Water)action).getAmount());
					plantDetails.append("ml, ");
					newLine = true;
				}

				if (((Water)action).getTemp() != null)
				{
					plantDetails.append("*Temp*: ");
					plantDetails.append(((Water)action).getTemp());
					plantDetails.append("ºC, ");
					newLine = true;
				}

				if (newLine)
				{
					plantDetails.append(NEW_LINE);
				}
			}

			if (!TextUtils.isEmpty(action.getNotes()))
			{
				plantDetails.append(action.getNotes());
				plantDetails.append(NEW_LINE);
			}
		}

		plantDetails.append("##Raw plant data");
		plantDetails.append(NEW_LINE);
		plantDetails.append("```").append(NEW_LINE).append(GsonHelper.parse(plant)).append(NEW_LINE).append("```");

		return null;
	}

	/**
	 * Returns a printable date from a timestmp
	 * @param context
	 * @param timestamp
	 * @return
	 */
	public static String printableDate(Context context, long timestamp)
	{
		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);

		return dateFormat.format(new Date(timestamp)) + " " + timeFormat.format(new Date(timestamp));
	}
}
