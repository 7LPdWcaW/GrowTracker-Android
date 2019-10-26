package me.anon.lib.export

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.github.mikephil.charting.charts.LineChart
import me.anon.grow.R
import me.anon.lib.TdsUnit
import me.anon.lib.TempUnit
import me.anon.lib.helper.NotificationHelper
import me.anon.lib.helper.StatsHelper
import me.anon.model.*
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * // TODO: Add class description
 */
class ExportHelper(
	val context: Context,
	val exportProcessor: Class<out ExportProcessor> = MarkdownProcessor::class.java,
	val includeImages: Boolean = true
)
{
	public fun exportPlants(plants: ArrayList<Plant>)
	{
		val intent = Intent(context, ExportService::class.java)
		intent.putStringArrayListExtra("plants", ArrayList(plants.map { it.id }))

		plants.firstOrNull()?.let {
			intent.putExtra("title", it.name.replace("[^a-zA-Z0-9]+".toRegex(), "-"))
			intent.putExtra("name", it.name)
		}

		intent.putExtra("include_images", includeImages)

		intent.putExtra("processor", exportProcessor)
		context.startService(intent)
	}

	public fun exportGarden(garden: Garden)
	{
		val intent = Intent(context, ExportService::class.java)
		intent.putStringArrayListExtra("plants", ArrayList(garden.plantIds))
		intent.putExtra("processor", exportProcessor)
		intent.putExtra("garden", garden)
		intent.putExtra("title", garden.name.replace("[^a-zA-Z0-9]+".toRegex(), "-"))
		intent.putExtra("name", garden.name)
		intent.putExtra("include_images", includeImages)
		context.startService(intent)
	}

	public fun executeExport(plants: ArrayList<Plant>, garden: Garden? = null, outputName: String, notificationTitle: String, callback: (File, Context) -> Unit)
	{
		val exportInt = "" + System.currentTimeMillis()

		val measureUnit = me.anon.lib.Unit.getSelectedMeasurementUnit(context)
		val deliveryUnit = me.anon.lib.Unit.getSelectedDeliveryUnit(context)
		val tempUnit = TempUnit.getSelectedTemperatureUnit(context)
		val tdsUnit = TdsUnit.getSelectedTdsUnit(context)
		val folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/GrowTracker/growlogs/"

		val exportFolder = File(folderPath)
		exportFolder.mkdirs()

		val finalFile = File(exportFolder, "$outputName.zip")
		if (finalFile.exists())
		{
			finalFile.delete()
		}

		try
		{
			val outFile = ZipFile(finalFile)
			val params = ZipParameters()
			params.compressionMethod = Zip4jConstants.COMP_DEFLATE
			params.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL

			garden?.let {
				val processor = exportProcessor.newInstance().apply {
					this.selectedDelivery = deliveryUnit
					this.selectedMeasurement = measureUnit
					this.selectedTemp = tempUnit
					this.selectedTds = tdsUnit
				}

				processor.beginDocument(false)

				// do processor stuff
				processor.printGardenDetails(it)
				processor.printGardenActions(it)
				processor.printGardenStats(it)

				// do chart stuff
				val humidityCount = it.actions.sumBy { if (it is HumidityChange) 1 else 0 }
				val tempCount = it.actions.sumBy { if (it is TemperatureChange) 1 else 0 }
				val humidityWidth = 1024 + (humidityCount * 20)
				val tempWidth = 1024 + (tempCount * 20)
				val height = 512

				saveGardenTempChart(tempWidth, height, garden, outFile)
				saveGardenHumdityChart(humidityWidth, height, garden, outFile)
				processor.endDocument(outFile)
			}

			plants.forEach { plant ->
				val processor = exportProcessor.newInstance().apply {
					this.selectedDelivery = deliveryUnit
					this.selectedMeasurement = measureUnit
					this.selectedTemp = tempUnit
					this.selectedTds = tdsUnit
				}
				processor.beginDocument()

				// temp folder to write to
				val zipPathPrefix = garden?.let { plant.name.replace("[^a-zA-Z0-9]+".toRegex(), "-") + "/"} ?: ""

				// do processor stuff
				processor.printPlantDetails(plant)
				processor.printPlantStages(plant)
				processor.printPlantStats(plant)
				processor.printPlantActions(plant)

				val imagePaths = arrayListOf<String>()
				for (filePath in plant.images!!)
				{
					try
					{
						val currentImage = File(filePath)
						var fileDate = java.lang.Long.parseLong(currentImage.name.replace("[^0-9]".toRegex(), ""))

						if (fileDate == 0L)
						{
							fileDate = currentImage.lastModified()
						}

						imagePaths.add(zipPathPrefix + "images/" + dateFolder(context, fileDate) + "/" + fileDate + ".jpg")
					}
					catch (e: java.lang.Exception)
					{
						e.printStackTrace()
					}
				}
				processor.printPlantImages(imagePaths)

				// do chart stuff
				val totalWater = plant.actions?.sumBy { if (it is Water) 1 else 0 } ?: 0
				val width = 1024 + (totalWater * 20)
				val height = 512

				saveTempChart(width, height, plant, zipPathPrefix, outFile)
				saveTdsCharts(width, height, plant, zipPathPrefix, outFile)
				saveInputPhChart(width, height, plant, zipPathPrefix, outFile)
				saveAdditiveChart(width, height, plant, zipPathPrefix, outFile)

				processor.printRaw(plant)
				processor.endDocument(outFile, zipPathPrefix)
			}

			// do image stuff
			if (includeImages)
			{
				copyImagesAndFinish(plants, garden, outFile, notificationTitle, callback)
			}
			else
			{
				callback(outFile.file, context)
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	@SuppressLint("StaticFieldLeak")
	private fun copyImagesAndFinish(plant: ArrayList<Plant>, garden: Garden?, finalFile: ZipFile, notificationTitle: String, callback: (File, Context) -> Unit)
	{
		val appContext = context.applicationContext

		object : AsyncTask<Plant?, Int?, File?>()
		{
			val usePrefix = garden != null
			protected val notificationManager: NotificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			protected lateinit var exportNotification: NotificationCompat.Builder

			override fun onPreExecute()
			{
				NotificationHelper.createExportChannel(appContext)

				exportNotification = NotificationCompat.Builder(appContext, "export")
					.setContentText(appContext.getString(R.string.exporting_start, notificationTitle))
					.setContentTitle(appContext.getString(R.string.export_progress))
					.setContentIntent(PendingIntent.getActivity(appContext, 0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
					.setTicker(appContext.getString(R.string.exporting_start, notificationTitle))
					.setSmallIcon(R.drawable.ic_stat_name)
					.setPriority(NotificationCompat.PRIORITY_LOW)
					.setSound(null)

				notificationManager.notify(0, exportNotification.build())
			}

			protected override fun onProgressUpdate(vararg values: Int?)
			{
				exportNotification.setProgress(values[1] ?: 0, values[0] ?: 0, false)
				notificationManager.notify(0, exportNotification.build())
			}

			override fun doInBackground(vararg params: Plant?): File?
			{
				var total = params.sumBy { it?.images?.size ?: 0 }

				var count = 0
				for (index in params.indices)
				{
					val plant = params[index]
					val zipPathPrefix = if (usePrefix) plant!!.name.replace("[^a-zA-Z0-9]+".toRegex(), "-") + "/" else ""

					// Copy images to dir
					plant?.images?.let {
						it.forEach { filePath ->
							try
							{
								val currentImage = File(filePath)
								var fileDate = java.lang.Long.parseLong(currentImage.name.replace("[^0-9]".toRegex(), ""))

								if (fileDate == 0L)
								{
									fileDate = currentImage.lastModified()
								}

								val parameters = ZipParameters()
								parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
								parameters.fileNameInZip = zipPathPrefix + "images/" + dateFolder(appContext, fileDate) + "/" + fileDate + ".jpg"
								parameters.isSourceExternalStream = true

								val fis = FileInputStream(currentImage)
								finalFile.addStream(fis, parameters)
							}
							catch (e: Exception)
							{
								e.printStackTrace()
							}

							publishProgress(++count, total)
						}
					}
				}

				publishProgress(total, total)
				return finalFile.file
			}

			override fun onPostExecute(file: File?)
			{
				file?.let {
					context?.let {
						val openIntent = Intent(Intent.ACTION_VIEW)
						val apkURI = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
						openIntent.setDataAndType(apkURI, "application/zip")
						openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

						exportNotification = NotificationCompat.Builder(context, "export")
							.setContentText(context.getString(R.string.exporting_path, notificationTitle, file.absolutePath))
							.setTicker(context.getString(R.string.exporting_complete, notificationTitle))
							.setContentTitle(context.getString(R.string.export_complete))
							.setContentIntent(PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT))
							.setStyle(NotificationCompat.BigTextStyle()
								.bigText(context.getString(R.string.exporting_path, notificationTitle, file.absolutePath))
							)
							.setSmallIcon(R.drawable.ic_stat_done)
							.setPriority(NotificationCompat.PRIORITY_HIGH)
							.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
							.setAutoCancel(true)
							.setSound(null)
							.setProgress(0, 0, false)

						notificationManager.notify(0, exportNotification.build())
					}

					callback(file, appContext)
				}
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *plant.toTypedArray())
	}

	private fun saveGardenTempChart(width: Int, height: Int, garden: Garden, outZip: ZipFile)
	{
		try
		{
			val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
			val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

			val temp = LineChart(context)
			temp.setExtraOffsets(30f, 30f, 30f, 30f)
			temp.setPadding(100, 100, 100, 100)
			temp.layoutParams = ViewGroup.LayoutParams(width, height)
			temp.minimumWidth = width
			temp.minimumHeight = height
			temp.measure(widthMeasureSpec, heightMeasureSpec)
			temp.requestLayout()
			temp.layout(0, 0, width, height)
			StatsHelper.setTempData(garden, context, temp, null)
			temp.data.setDrawValues(true)

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = "garden-temp.jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				temp.chartBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
				val stream = ByteArrayInputStream(outputStream.toByteArray())
				outZip.addStream(stream, parameters)

				stream.close()
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	private fun saveGardenHumdityChart(width: Int, height: Int, garden: Garden, outZip: ZipFile)
	{
		try
		{
			val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
			val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

			val chart = LineChart(context)
			chart.setExtraOffsets(30f, 30f, 30f, 30f)
			chart.setPadding(100, 100, 100, 100)
			chart.layoutParams = ViewGroup.LayoutParams(width, height)
			chart.minimumWidth = width
			chart.minimumHeight = height
			chart.measure(widthMeasureSpec, heightMeasureSpec)
			chart.requestLayout()
			chart.layout(0, 0, width, height)
			StatsHelper.setHumidityData(garden, context, chart, null)
			chart.data.setDrawValues(true)

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = "garden-humidity.jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				chart.chartBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
				val stream = ByteArrayInputStream(outputStream.toByteArray())
				outZip.addStream(stream, parameters)

				stream.close()
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	private fun saveTempChart(width: Int, height: Int, plant: Plant, pathPrefix: String, outZip: ZipFile)
	{
		try
		{
			val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
			val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

			val temp = LineChart(context)
			temp.setExtraOffsets(30f, 30f, 30f, 30f)
			temp.setPadding(100, 100, 100, 100)
			temp.layoutParams = ViewGroup.LayoutParams(width, height)
			temp.minimumWidth = width
			temp.minimumHeight = height
			temp.measure(widthMeasureSpec, heightMeasureSpec)
			temp.requestLayout()
			temp.layout(0, 0, width, height)
			StatsHelper.setTempData(plant, context, temp, null)
			temp.data.setDrawValues(true)

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = pathPrefix + "temp.jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				temp.chartBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
				val stream = ByteArrayInputStream(outputStream.toByteArray())
				outZip.addStream(stream, parameters)

				stream.close()
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	private fun saveTdsCharts(width: Int, height: Int, plant: Plant, pathPrefix: String, outZip: ZipFile)
	{
		val tdsNames = TreeSet<TdsUnit>()
		for (action in plant.actions!!)
		{
			if (action is Water && action.tds != null)
			{
				tdsNames.add(action.tds!!.type)
			}
		}

		val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
		val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

		for (tdsName in tdsNames)
		{
			val tds = LineChart(context)
			tds.setExtraOffsets(30f, 30f, 30f, 30f)
			tds.setPadding(100, 100, 100, 100)
			tds.layoutParams = ViewGroup.LayoutParams(width, height)
			tds.minimumWidth = width
			tds.minimumHeight = height
			tds.measure(widthMeasureSpec, heightMeasureSpec)
			tds.requestLayout()
			tds.layout(0, 0, width, height)
			StatsHelper.setTdsData(plant, context, tds, null, tdsName)
			tds.data.setDrawValues(true)

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = pathPrefix + tdsName.enStr + ".jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				tds.chartBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
				val stream = ByteArrayInputStream(outputStream.toByteArray())
				outZip.addStream(stream, parameters)

				stream.close()
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
		}
	}

	private fun saveInputPhChart(width: Int, height: Int, plant: Plant, pathPrefix: String, outZip: ZipFile)
	{
		try
		{
			val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
			val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

			val inputPh = LineChart(context)
			inputPh.setExtraOffsets(30f, 30f, 30f, 30f)
			inputPh.setPadding(100, 100, 100, 100)
			inputPh.layoutParams = ViewGroup.LayoutParams(width, height)
			inputPh.minimumWidth = width
			inputPh.minimumHeight = height
			inputPh.measure(widthMeasureSpec, heightMeasureSpec)
			inputPh.requestLayout()
			inputPh.layout(0, 0, width, height)
			StatsHelper.setInputData(plant, context, inputPh, null)
			inputPh.data.setDrawValues(true)

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = pathPrefix + "input-ph.jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				inputPh.chartBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
				val stream = ByteArrayInputStream(outputStream.toByteArray())
				outZip.addStream(stream, parameters)

				stream.close()
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	private fun saveAdditiveChart(width: Int, height: Int, plant: Plant, pathPrefix: String, outZip: ZipFile)
	{
		try
		{
			val additiveNames = hashSetOf<String>()
			plant.actions?.filter { it is Water }?.forEach {
				additiveNames.addAll((it as Water).additives.map { it.description ?: "" })
			}
			additiveNames.removeAll { it == "" }

			val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
			val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

			val additives = LineChart(context)
			additives.setExtraOffsets(30f, 30f, 30f, 30f)
			additives.setPadding(100, 100, 100, 100)
			additives.layoutParams = ViewGroup.LayoutParams(width, height)
			additives.minimumWidth = width
			additives.minimumHeight = height
			additives.measure(widthMeasureSpec, heightMeasureSpec)
			additives.requestLayout()
			additives.layout(0, 0, width, height)
			StatsHelper.setAdditiveData(plant, context, additives, additiveNames)
			additives.data.setDrawValues(true)

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = pathPrefix + "additives.jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				additives.chartBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
				val stream = ByteArrayInputStream(outputStream.toByteArray())
				outZip.addStream(stream, parameters)

				stream.close()
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
		}
		catch (e: java.lang.Exception)
		{
			e.printStackTrace()
		}
	}

	companion object
	{
		/**
		 * Returns a string suitable for folders based on timestamp
		 * @param context
		 * @param timestamp
		 * @return
		 */
		@JvmStatic
		fun dateFolder(context: Context, timestamp: Long): String
		{
			val dateFormat = android.text.format.DateFormat.getDateFormat(context)
			return dateFormat.format(Date(timestamp)).replace("[^0-9]".toRegex(), "-")
		}
	}
}
