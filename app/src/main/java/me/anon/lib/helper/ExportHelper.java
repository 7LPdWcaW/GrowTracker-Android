package me.anon.lib.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import me.anon.grow.R;
import me.anon.lib.ExportCallback;
import me.anon.lib.Unit;
import me.anon.model.Action;
import me.anon.model.Additive;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
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
	 */
	@Nullable public static void exportPlants(final Context context, @NonNull final ArrayList<Plant> plants, String name, final ExportCallback callback)
	{
		String exportInt = "" + System.currentTimeMillis();
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

		File exportFolder = new File(folderPath + "/GrowLogs/" + exportInt);
		exportFolder.mkdirs();

		final File finalFile = new File(folderPath + "/GrowLogs/" + name + ".zip");

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
				final String zipPathPrefix = plants.size() == 1 ? "" : plant.getName() + "/";

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
				plantDetails.append("# ").append(plant.getName()).append(" Grow Log");
				plantDetails.append(NEW_LINE);
				plantDetails.append("*Strain*: ").append(plant.getStrain());
				plantDetails.append(NEW_LINE);
				plantDetails.append("*Is clone?*: ").append(plant.getClone());
				plantDetails.append(NEW_LINE);
				plantDetails.append("*Medium*: ").append(plant.getMedium().getPrintString());
				plantDetails.append(NEW_LINE);

				plantDetails.append("## Stages");
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

				plantDetails.append("## General stats");
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
				StatsHelper.setInputData(plant, context, null, avePh);
				plantDetails.append(" - *Minimum input pH*: ").append(avePh[0]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Maximum input pH*: ").append(avePh[1]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Average input pH*: ").append(avePh[2]);
				plantDetails.append(NEW_LINE);

				String[] avePpm = new String[3];
				StatsHelper.setPpmData(plant, context, null, avePpm, usingEc);
				plantDetails.append(" - *Minimum input " + (usingEc ? "EC" : "ppm") + "*: ").append(avePpm[0]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Maximum input " + (usingEc ? "EC" : "ppm") + "*: ").append(avePpm[1]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Average input " + (usingEc ? "EC" : "ppm") + "*: ").append(avePpm[2]);
				plantDetails.append(NEW_LINE);

				String[] aveTemp = new String[3];
				StatsHelper.setTempData(plant, context, null, aveTemp);
				plantDetails.append(" - *Minimum input temperature*: ").append(aveTemp[0]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Maximum input temperature*: ").append(aveTemp[1]);
				plantDetails.append(NEW_LINE);
				plantDetails.append(" - *Average input temperature*: ").append(aveTemp[2]);
				plantDetails.append(NEW_LINE);

				if (!additiveNames.isEmpty())
				{
					plantDetails.append("## Additives used");
					plantDetails.append(NEW_LINE);
					plantDetails.append(" - ");
					plantDetails.append(TextUtils.join(NEW_LINE + " - ", additiveNames));
					plantDetails.append(NEW_LINE);
				}

				plantDetails.append("## Timeline");
				plantDetails.append(NEW_LINE);

				ArrayList<Action> actions = plant.getActions();
				for (int i = actions.size() - 1; i >= 0; i--)
				{
					Action action = actions.get(i);
					plantDetails.append("### ").append(printableDate(context, action.getDate()));
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
								if (additive == null || additive.getAmount() == null)
								{
									continue;
								}

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

				plantDetails.append("## Raw plant data");
				plantDetails.append(NEW_LINE);
				plantDetails.append("```").append("\r\n").append(MoshiHelper.toJson(plant, Plant.class)).append("\r\n").append("```");
				plantDetails.append(NEW_LINE);
				plantDetails.append("Generated using [Grow Tracker](https://github.com/7LPdWcaW/GrowTracker-Android)");

				// Write the log
				try
				{
					ZipParameters parameters = new ZipParameters();
					parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
					parameters.setFileNameInZip(zipPathPrefix + "growlog.md");
					parameters.setSourceExternalStream(true);

					outFile.addStream(new ByteArrayInputStream(plantDetails.toString().getBytes("UTF-8")), parameters);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					int width = 1024 + (totalWater * 20);
					int height = 512;
					int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
					int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

					LineChart additives = new LineChart(context);
					additives.setPadding(100, 100, 100, 100);
					additives.setLayoutParams(new ViewGroup.LayoutParams(width, height));
					additives.setMinimumWidth(width);
					additives.setMinimumHeight(height);
					additives.measure(widthMeasureSpec, heightMeasureSpec);
					additives.requestLayout();
					additives.layout(0, 0, width, height);
					StatsHelper.setAdditiveData(plant, context, additives, additiveNames);
					additives.getData().setDrawValues(true);

					try
					{
						ZipParameters parameters = new ZipParameters();
						parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
						parameters.setFileNameInZip(zipPathPrefix + "additives.jpg");
						parameters.setSourceExternalStream(true);

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						additives.getChartBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
						ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
						outFile.addStream(stream, parameters);

						stream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					LineChart inputPh = new LineChart(context);
					inputPh.setPadding(100, 100, 100, 100);
					inputPh.setLayoutParams(new ViewGroup.LayoutParams(width, height));
					inputPh.setMinimumWidth(width);
					inputPh.setMinimumHeight(height);
					inputPh.measure(widthMeasureSpec, heightMeasureSpec);
					inputPh.requestLayout();
					inputPh.layout(0, 0, width, height);
					StatsHelper.setInputData(plant, context, inputPh, null);
					inputPh.getData().setDrawValues(true);

					try
					{
						ZipParameters parameters = new ZipParameters();
						parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
						parameters.setFileNameInZip(zipPathPrefix + "input-ph.jpg");
						parameters.setSourceExternalStream(true);

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						inputPh.getChartBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
						ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
						outFile.addStream(stream, parameters);

						stream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					LineChart ppm = new LineChart(context);
					ppm.setPadding(100, 100, 100, 100);
					ppm.setLayoutParams(new ViewGroup.LayoutParams(width, height));
					ppm.setMinimumWidth(width);
					ppm.setMinimumHeight(height);
					ppm.measure(widthMeasureSpec, heightMeasureSpec);
					ppm.requestLayout();
					ppm.layout(0, 0, width, height);
					StatsHelper.setPpmData(plant, context, ppm, null, usingEc);
					ppm.getData().setDrawValues(true);

					try
					{
						ZipParameters parameters = new ZipParameters();
						parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
						parameters.setFileNameInZip(zipPathPrefix + (usingEc ? "ec" : "ppm") + ".jpg");
						parameters.setSourceExternalStream(true);

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						ppm.getChartBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
						ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
						outFile.addStream(stream, parameters);

						stream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					LineChart temp = new LineChart(context);
					temp.setPadding(100, 100, 100, 100);
					temp.setLayoutParams(new ViewGroup.LayoutParams(width, height));
					temp.setMinimumWidth(width);
					temp.setMinimumHeight(height);
					temp.measure(widthMeasureSpec, heightMeasureSpec);
					temp.requestLayout();
					temp.layout(0, 0, width, height);
					StatsHelper.setTempData(plant, context, temp, null);
					temp.getData().setDrawValues(true);

					try
					{
						ZipParameters parameters = new ZipParameters();
						parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
						parameters.setFileNameInZip(zipPathPrefix + "temp.jpg");
						parameters.setSourceExternalStream(true);

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						temp.getChartBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
						ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
						outFile.addStream(stream, parameters);

						stream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			copyImagesAndFinish(context, plants, outFile, callback);
		}
		catch (ZipException e)
		{
			e.printStackTrace();
		}
	}

	protected static void copyImagesAndFinish(Context context, final ArrayList<Plant> plant, final ZipFile finalFile, final ExportCallback callback)
	{
		final Context appContext = context.getApplicationContext();
		new AsyncTask<Plant, Integer, File>()
		{
			protected NotificationCompat.Builder exportNotification;
			protected NotificationManager notificationManager;

			@Override protected void onPreExecute()
			{
				NotificationHelper.createExportChannel(appContext);

				notificationManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);

				exportNotification = new NotificationCompat.Builder(appContext, "export")
					.setContentText("Exporting grow log for " + (plant.size() == 1 ? plant.get(0).getName() : "multiple plants"))
					.setContentTitle("Exporting")
					.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
					.setTicker("Exporting grow log for " + (plant.size() == 1 ? plant.get(0).getName() : "multiple plants"))
					.setSmallIcon(R.drawable.ic_stat_name)
					.setPriority(NotificationCompat.PRIORITY_LOW)
					.setSound(null);

				notificationManager.notify(0, exportNotification.build());
			}

			@Override protected void onProgressUpdate(Integer... values)
			{
				if (values[1] == values[0])
				{
					notificationManager.cancel(0);
				}
				else
				{
					exportNotification.setProgress(values[1], values[0], false);
					notificationManager.notify(0, exportNotification.build());
				}
			}

			@Override protected File doInBackground(Plant... params)
			{
				int total = 0;
				for (Plant plant : plant)
				{
					total += plant.getImages().size();
				}

				int count = 0;
				for (int index = 0; index < params.length; index++)
				{
					Plant plant = params[index];
					final String zipPathPrefix = params.length == 1 ? "" : plant.getName() + "/";

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

							ZipParameters parameters = new ZipParameters();
							parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
							parameters.setFileNameInZip(zipPathPrefix + "images/" + dateFolder(appContext, fileDate) + "/" + fileDate + ".jpg");
							parameters.setSourceExternalStream(true);

							FileInputStream fis = new FileInputStream(currentImage);
							finalFile.addStream(fis, parameters);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						publishProgress(++count, total);
					}
				}

				notificationManager.cancel(0);
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
