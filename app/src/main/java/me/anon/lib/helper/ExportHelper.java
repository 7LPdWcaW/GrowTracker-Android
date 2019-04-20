package me.anon.lib.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import me.anon.grow.R;
import me.anon.lib.ExportCallback;
import me.anon.lib.Unit;
import me.anon.lib.manager.PlantManager;
import me.anon.model.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

import static me.anon.lib.Unit.ML;

/**
 * Helper class for exporting plant data into a Tarball file
 */
public class ExportHelper
{
	private static final String NEW_LINE = "\r\n\r\n";

	/**
	 * Creates a grow log for given plant and zips them into a compressed file
	 */
	@Nullable public static void exportPlants(final Context context, @NonNull final ArrayList<Plant> plants, String name, final ExportCallback callback)
	{
		String folderPath = "";
		Unit measureUnit = Unit.getSelectedMeasurementUnit(context);
		Unit deliveryUnit = Unit.getSelectedDeliveryUnit(context);
		boolean usingEc = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("tds_ec", false);

		if (Environment.getExternalStorageDirectory() != null)
		{
			folderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		else
		{
			folderPath = Environment.DIRECTORY_DOWNLOADS;
		}

		ArrayList<File> outputs = new ArrayList<>();
		File exportFolder = new File(folderPath + "/GrowLogs/");
		exportFolder.mkdirs();

		final File finalFile = new File(exportFolder.getAbsolutePath() + "/" + name + ".zip");

		if (finalFile.exists())
		{
			finalFile.delete();
		}

		try
		{
			final ZipFile outFile = new ZipFile(finalFile);
			final ZipParameters params = new ZipParameters();
			params.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			params.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			for (Plant plant : plants)
			{
				// temp folder to write to
				final File tempFolder = new File(exportFolder.getAbsolutePath() + "/" + plant.getId());
				outputs.add(tempFolder);

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
				final Set<String> additiveNames = new HashSet<>();

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

						List<Additive> actionAdditives = ((Water)action).getAdditives();
						for (Additive additive : actionAdditives)
						{
							additiveNames.add(additive.getDescription());
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
				StatsHelper.setPpmData(plant, null, avePpm, usingEc);
				plantDetails.append(" - *Minimum input " + (usingEc ? "EC" : "ppm") + "*: ").append(avePpm[0]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Maximum input " + (usingEc ? "EC" : "ppm") + "*: ").append(avePpm[1]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Average input " + (usingEc ? "EC" : "ppm") + "*: ").append(avePpm[2]);
				plantDetails.append(NEW_LINE);

				String[] aveTemp = new String[3];
				StatsHelper.setTempData(plant, null, aveTemp);
				plantDetails.append(" - *Minimum input temperature*: ").append(aveTemp[0]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Maximum input temperature*: ").append(aveTemp[1]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Average input temperature*: ").append(aveTemp[2]);
				plantDetails.append(NEW_LINE);

				if (!additiveNames.isEmpty())
				{
					plantDetails.append("##Additives used");
					plantDetails.append(NEW_LINE);
					TextUtils.join(NEW_LINE + " - ", additiveNames);
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
					if (plants.size() > 0)
					{
						params.setRootFolderInZip(plant.getName());
					}

					outFile.addFile(new File(tempFolder.getAbsolutePath() + "/" + plant.getName() + " - growlog.md"), params);

					if (new File(tempFolder.getAbsolutePath() + "/images/").exists())
					{
						outFile.addFolder(new File(tempFolder.getAbsolutePath() + "/images/"), params);
					}

					int width = 512 + (totalWater * 10);
					int height = 512;
					int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
					int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

					LineChart additives = new LineChart(context);
					additives.setLayoutParams(new ViewGroup.LayoutParams(width, height));
					additives.setMinimumWidth(width);
					additives.setMinimumHeight(height);
					additives.measure(widthMeasureSpec, heightMeasureSpec);
					additives.requestLayout();
					additives.layout(0, 0, width, height);
					StatsHelper.setAdditiveData(plant, additives, additiveNames);
					additives.getData().setDrawValues(true);

					try
					{
						OutputStream stream = new FileOutputStream(tempFolder.getAbsolutePath() + "/additives.png");
						additives.getChartBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

						stream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					LineChart inputPh = new LineChart(context);
					inputPh.setLayoutParams(new ViewGroup.LayoutParams(width, height));
					inputPh.setMinimumWidth(width);
					inputPh.setMinimumHeight(height);
					inputPh.measure(widthMeasureSpec, heightMeasureSpec);
					inputPh.requestLayout();
					inputPh.layout(0, 0, width, height);
					StatsHelper.setInputData(plant, inputPh, null);
					inputPh.getData().setDrawValues(true);

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
					StatsHelper.setPpmData(plant, ppm, null, usingEc);
					ppm.getData().setDrawValues(true);

					try
					{
						OutputStream stream = new FileOutputStream(tempFolder.getAbsolutePath() + "/" + (usingEc ? "ec" : "ppm") + ".png");
						ppm.getChartBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

						stream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					LineChart temp = new LineChart(context);
					temp.setLayoutParams(new ViewGroup.LayoutParams(width, height));
					temp.setMinimumWidth(width);
					temp.setMinimumHeight(height);
					temp.measure(widthMeasureSpec, heightMeasureSpec);
					temp.requestLayout();
					temp.layout(0, 0, width, height);
					StatsHelper.setTempData(plant, temp, null);
					temp.getData().setDrawValues(true);

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

					if (new File(tempFolder.getAbsolutePath() + "/additives.png").exists())
					{
						outFile.addFile(new File(tempFolder.getAbsolutePath() + "/additives.png"), params);
					}

					if (new File(tempFolder.getAbsolutePath() + "/input-ph.png").exists())
					{
						outFile.addFile(new File(tempFolder.getAbsolutePath() + "/input-ph.png"), params);
					}

					if (new File(tempFolder.getAbsolutePath() + "/ppm.png").exists())
					{
						outFile.addFile(new File(tempFolder.getAbsolutePath() + "/ppm.png"), params);
					}

					if (new File(tempFolder.getAbsolutePath() + "/ec.png").exists())
					{
						outFile.addFile(new File(tempFolder.getAbsolutePath() + "/ec.png"), params);
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


			}

			copyImagesAndFinish(context, plants, outputs, outFile, callback);
		}
		catch (ZipException e)
		{
			e.printStackTrace();
		}
	}

	protected static void copyImagesAndFinish(Context context, final ArrayList<Plant> plant, final ArrayList<File> outputs, final ZipFile finalFile, final ExportCallback callback)
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

				if (Build.VERSION.SDK_INT >= 26)
				{
					NotificationChannel channel = new NotificationChannel("export", "Export status", NotificationManager.IMPORTANCE_DEFAULT);
					channel.setSound(null, null);
					channel.enableLights(false);
					channel.setLightColor(Color.BLUE);
					channel.enableVibration(false);
					notificationManager.createNotificationChannel(channel);
				}

				notificationManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);

				exportNotification = new NotificationCompat.Builder(appContext, "export")
					.setContentText("Exporting grow log for " + (plant.size() == 1 ? plant.get(0).getName() : "multiple plants"))
					.setContentTitle("Exporting")
					.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
					.setTicker("Exporting grow log for " + (plant.size() == 1 ? plant.get(0).getName() : "multiple plants"))
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setSmallIcon(R.drawable.ic_stat_name);

				notificationManager.notify(0, exportNotification.build());
			}

			@Override protected void onProgressUpdate(Integer... values)
			{
				exportNotification.setPriority(NotificationCompat.PRIORITY_LOW);
				exportNotification.setProgress(values[1], values[0], false);
				notificationManager.notify(0, exportNotification.build());
			}

			@Override protected File doInBackground(Plant... params)
			{
				if (params.length != outputs.size())
				{
					throw new IllegalArgumentException("plant size did not match output size");
				}

				int total = 0;
				for (Plant plant : plant)
				{
					total += plant.getImages().size();
				}

				int count = 0;

				for (int index = 0; index < params.length; index++)
				{
					Plant plant = params[index];
					File tempFolder = outputs.get(index);

					// Copy images to dir
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

							fis.close();
							fos.flush();
							fos.close();

							final ZipParameters zipParams = new ZipParameters();
							zipParams.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
							zipParams.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
							finalFile.addFolder(imageFolderPath, zipParams);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						publishProgress(++count, total);
					}

					deleteRecursive(tempFolder);
				}

				callback.onCallback(appContext, finalFile.getFile());
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, plant.toArray(new Plant[0]));
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
