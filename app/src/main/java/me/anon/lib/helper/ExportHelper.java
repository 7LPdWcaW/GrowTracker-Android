package me.anon.lib.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

import me.anon.grow.R;
import me.anon.lib.ExportCallback;
import me.anon.lib.Unit;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantMedium;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;

import static me.anon.lib.Unit.ML;

/**
 * Helper class for exporting plant data into a Tarball file
 */
public class ExportHelper
{
	private static final String NEW_LINE = "\r\n\r\n";

	/**
	 * Creates a grow log for given plant and zips them into a compressed file
	 *
	 * @param context
	 * @param plant
	 * @param callback
	 *
	 * @return
	 */
	@Nullable public static void exportPlant(final Context context, @NonNull final Plant plant, final ExportCallback callback)
	{
		String folderPath = "";
		Unit measureUnit = Unit.getSelectedMeasurementUnit(context);
		Unit deliveryUnit = Unit.getSelectedDeliveryUnit(context);

		if (Environment.getExternalStorageDirectory() != null)
		{
			folderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		else
		{
			folderPath = Environment.DIRECTORY_DOWNLOADS;
		}

		File exportFolder = new File(folderPath + "/GrowLogs/");
		exportFolder.mkdirs();

		// temp folder to write to
		final File tempFolder = new File(exportFolder.getAbsolutePath() + "/" + plant.getId());

		if (tempFolder.exists())
		{
			deleteRecursive(tempFolder);
		}

		tempFolder.mkdir();

		long startDate = plant.getPlantDate();
		long endDate = System.currentTimeMillis();
		long feedDifference = 0L;
		long waterDifference = 0L;
		long lastWater = 0L;
		int totalWater = 0, totalFlush = 0;

		for (Action action : plant.getActions())
		{
			if (action instanceof StageChange)
			{
				if (((StageChange)action).getNewStage() == PlantStage.HARVESTED)
				{
					endDate = action.getDate();
				}
			}

			if (action.getClass() == Water.class)
			{
				if (lastWater != 0)
				{
					waterDifference += Math.abs(action.getDate() - lastWater);
				}

				totalWater++;
				lastWater = action.getDate();
			}

			if (action instanceof EmptyAction && ((EmptyAction)action).getAction() == Action.ActionName.FLUSH)
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
		Map<PlantStage, Action> plantStages = plant.getStages();

		for (PlantStage plantStage : stages.keySet())
		{
			plantDetails.append("- *").append(plantStage.getPrintString()).append("*: ");
			plantDetails.append(printableDate(context, plantStages.get(plantStage).getDate()));

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
		plantDetails.append(" - *Total waters*: ").append(String.valueOf(totalWater));
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Total flushes*: ").append(String.valueOf(totalFlush));
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Average time between waterings*: ").append(String.format("%1$,.2f days", (TimeHelper.toDays(waterDifference) / (double)totalWater)));
		plantDetails.append(NEW_LINE);

		String[] avePh = new String[3];
		StatsHelper.setInputData(plant, null, avePh);
		plantDetails.append(" - *Minimum input pH*: ").append(avePh[0]);
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Maximum input pH*: ").append(avePh[1]);
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Average input pH*: ").append(avePh[2]);
		plantDetails.append(NEW_LINE);

		String[] avePpm = new String[3];
		StatsHelper.setPpmData(plant, null, avePpm);
		plantDetails.append(" - *Minimum input ppm*: ").append(avePpm[0]);
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Maximum input ppm*: ").append(avePpm[1]);
		plantDetails.append(NEW_LINE);
		plantDetails.append(" - *Average input ppm*: ").append(avePpm[2]);
		plantDetails.append(NEW_LINE);

		if (plant.getMedium() == PlantMedium.HYDRO)
		{
			String[] aveTemp = new String[3];
			StatsHelper.setTempData(plant, null, aveTemp);
			plantDetails.append(" - *Minimum input temperature*: ").append(aveTemp[0]);
			plantDetails.append(NEW_LINE);
			plantDetails.append(" - *Maximum input temperature*: ").append(aveTemp[1]);
			plantDetails.append(NEW_LINE);
			plantDetails.append(" - *Average input temperature*: ").append(aveTemp[2]);
			plantDetails.append(NEW_LINE);
		}

		plantDetails.append("##Timeline");
		plantDetails.append(NEW_LINE);

		for (Action action : plant.getActions())
		{
			plantDetails.append("###").append(printableDate(context, action.getDate()));
			plantDetails.append(NEW_LINE);

			if (action.getClass() == Water.class)
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
					plantDetails.append(ML.to(deliveryUnit, ((Water)action).getAmount()));
					plantDetails.append(deliveryUnit.getLabel());
					plantDetails.append(", ");
					newLine = true;
				}

				if (((Water)action).getTemp() != null)
				{
					plantDetails.append("*Temp*: ");
					plantDetails.append(((Water)action).getTemp());
					plantDetails.append("ºC, ");
					newLine = true;
				}

				if (((Water)action).getAdditives().size() > 0)
				{
					plantDetails.append(NEW_LINE);
					plantDetails.append("*Additives:*");
					plantDetails.append("\r\n");

					for (Additive additive : ((Water)action).getAdditives())
					{
						if (additive == null || additive.getAmount() == null) continue;

						double converted = ML.to(measureUnit, additive.getAmount());
						String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

						plantDetails.append("\r\n");
						plantDetails.append(" - ");
						plantDetails.append(additive.getDescription());
						plantDetails.append("  -  ");
						plantDetails.append(amountStr);
						plantDetails.append(measureUnit.getLabel());
						plantDetails.append("/");
						plantDetails.append(deliveryUnit.getLabel());
					}
				}

				if (newLine)
				{
					plantDetails.delete(plantDetails.length() - 2, plantDetails.length() - 1);
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
		plantDetails.append("```").append("\r\n").append(GsonHelper.parse(plant)).append("\r\n").append("```");
		plantDetails.append(NEW_LINE);
		plantDetails.append("Generated using [Grow Tracker](https://github.com/7LPdWcaW/GrowTracker-Android)");

		// Write the log
		try
		{
			FileWriter fileWriter = new FileWriter(tempFolder.getAbsolutePath() + "/growlog.md", false);
			fileWriter.write(plantDetails.toString());
			fileWriter.flush();
			fileWriter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// Create stats charts and save images
			final File finalFile = new File(exportFolder.getAbsolutePath() + "/" + plant.getName().replaceAll("[^a-zA-Z0-9]+", "-") + ".zip");

			if (finalFile.exists())
			{
				finalFile.delete();
			}

			final ZipFile outFile = new ZipFile(finalFile);
			final ZipParameters params = new ZipParameters();
			params.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			params.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			outFile.addFile(new File(tempFolder.getAbsolutePath() + "/growlog.md"), params);

			if (new File(tempFolder.getAbsolutePath() + "/images/").exists())
			{
				outFile.addFolder(new File(tempFolder.getAbsolutePath() + "/images/"), params);
			}

			int width = 512 + (totalWater * 10);
			int height = 512;
			int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
			int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

			LineChart inputPh = new LineChart(context);
			inputPh.setLayoutParams(new ViewGroup.LayoutParams(width, height));
			inputPh.setMinimumWidth(width);
			inputPh.setMinimumHeight(height);
			inputPh.measure(widthMeasureSpec, heightMeasureSpec);
			inputPh.requestLayout();
			inputPh.layout(0, 0, width, height);
			StatsHelper.setInputData(plant, inputPh, null);

			try
			{
				OutputStream stream = new FileOutputStream(tempFolder.getAbsolutePath() + "/input-ph.png");
				inputPh.getChartBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

				stream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			LineChart ppm = new LineChart(context);
			ppm.setLayoutParams(new ViewGroup.LayoutParams(width, height));
			ppm.setMinimumWidth(width);
			ppm.setMinimumHeight(height);
			ppm.measure(widthMeasureSpec, heightMeasureSpec);
			ppm.requestLayout();
			ppm.layout(0, 0, width, height);
			StatsHelper.setPpmData(plant, ppm, null);

			try
			{
				OutputStream stream = new FileOutputStream(tempFolder.getAbsolutePath() + "/ppm.png");
				ppm.getChartBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

				stream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (plant.getMedium() == PlantMedium.HYDRO)
			{
				LineChart temp = new LineChart(context);
				temp.setLayoutParams(new ViewGroup.LayoutParams(width, height));
				temp.setMinimumWidth(width);
				temp.setMinimumHeight(height);
				temp.measure(widthMeasureSpec, heightMeasureSpec);
				temp.requestLayout();
				temp.layout(0, 0, width, height);
				StatsHelper.setTempData(plant, temp, null);

				try
				{
					OutputStream stream = new FileOutputStream(tempFolder.getAbsolutePath() + "/temp.png");
					temp.getChartBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

					stream.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				if (new File(tempFolder.getAbsolutePath() + "/input-ph.png").exists())
				{
					outFile.addFile(new File(tempFolder.getAbsolutePath() + "/input-ph.png"), params);
				}

				if (new File(tempFolder.getAbsolutePath() + "/ppm.png").exists())
				{
					outFile.addFile(new File(tempFolder.getAbsolutePath() + "/ppm.png"), params);
				}

				if (new File(tempFolder.getAbsolutePath() + "/temp.png").exists())
				{
					outFile.addFile(new File(tempFolder.getAbsolutePath() + "/temp.png"), params);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			copyImagesAndFinish(context, plant, tempFolder, finalFile, callback);
		}
		catch (ZipException e)
		{
			e.printStackTrace();
		}
	}

	protected static void copyImagesAndFinish(Context context, final Plant plant, final File tempFolder, final File finalFile, final ExportCallback callback)
	{
		final Context appContext = context.getApplicationContext();
		new AsyncTask<Plant, Integer, File>()
		{
			protected NotificationCompat.Builder exportNotification;
			protected NotificationManager notificationManager;
			protected int plantIndex;

			@Override protected void onPreExecute()
			{
				plantIndex = PlantManager.getInstance().getPlants().indexOf(plant);
				notificationManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);

				exportNotification = new NotificationCompat.Builder(appContext)
					.setContentText("Exporting grow log for " + plant.getName())
					.setContentTitle("Exporting")
					.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
					.setTicker("Exporting grow log for " + plant.getName())
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setSmallIcon(R.drawable.ic_stat_name);

				notificationManager.notify(plantIndex, exportNotification.build());
			}

			@Override protected void onProgressUpdate(Integer... values)
			{
				exportNotification.setProgress(values[1], values[0], false);
				notificationManager.notify(plantIndex, exportNotification.build());
			}

			@Override protected File doInBackground(Plant... params)
			{
				Plant plant = params[0];

				// Copy images to dir
				int count = 0, total = plant.getImages().size();
				for (String filePath : plant.getImages())
				{
					try
					{
						File currentImage = new File(filePath);
						long fileDate = Long.parseLong(currentImage.getName().replaceAll("[^0-9]", ""));

						if (fileDate == 0)
						{
							fileDate = currentImage.lastModified();
						}

						File imageFolderPath = new File(tempFolder.getAbsolutePath() + "/images/" + dateFolder(appContext, fileDate) + "/");
						imageFolderPath.mkdirs();

						FileInputStream fis = new FileInputStream(currentImage);
						FileOutputStream fos = new FileOutputStream(new File(imageFolderPath.getAbsolutePath() + "/" + fileDate + ".jpg"));

						byte[] buffer = new byte[8192];
						int len = 0;

						while ((len = fis.read(buffer)) != -1)
						{
							fos.write(buffer, 0, len);
						}

						publishProgress(++count, total);

						fis.close();
						fos.flush();
						fos.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				deleteRecursive(tempFolder);
				callback.onCallback(appContext, finalFile);

				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, plant);
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

	/**
	 * Returns a string suitable for folders based on timestamp
	 * @param context
	 * @param timestamp
	 * @return
	 */
	public static String dateFolder(Context context, long timestamp)
	{
		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);

		return dateFormat.format(new Date(timestamp)).replaceAll("[^0-9]", "-");
	}

	public static void deleteRecursive(File fileOrDirectory)
	{
		if (fileOrDirectory.isDirectory())
		{
			File[] files = fileOrDirectory.listFiles();

			if (files != null)
			{
				for (File child : files)
				{
					deleteRecursive(child);
				}
			}
		}

		fileOrDirectory.delete();
	}
}
