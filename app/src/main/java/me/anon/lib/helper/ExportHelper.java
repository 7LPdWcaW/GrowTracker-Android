package me.anon.lib.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import me.anon.model.Plant;

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
		StringBuffer plantDetails = new StringBuffer(1000);
		plantDetails.append("#").append(plant.getName()).append(" Grow Log");
		plantDetails.append(NEW_LINE);
		plantDetails.append("*Strain*: ").append(plant.getStrain());
		plantDetails.append(NEW_LINE);
		plantDetails.append("*Planted*: ").append(printableDate(context, plant.getPlantDate()));

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
