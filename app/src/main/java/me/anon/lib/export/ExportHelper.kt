package me.anon.lib.export

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.statistics2_view.*
import me.anon.grow.R
import me.anon.grow.fragment.StatisticsFragment2
import me.anon.lib.TdsUnit
import me.anon.lib.TempUnit
import me.anon.lib.ext.*
import me.anon.lib.helper.NotificationHelper
import me.anon.lib.helper.StatsHelper
import me.anon.lib.helper.TimeHelper
import me.anon.model.*
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * // TODO: Add class description
 */
class ExportHelper(
	val context: Context,
	val exportProcessor: Class<out ExportProcessor> = MarkdownProcessor::class.java,
	val includeImages: Boolean = true
)
{
	private val statsColours by lazy {
		context.resources.getStringArray(R.array.stats_colours).map {
			Color.parseColor(it)
		}
	}

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
				val processor = exportProcessor.newInstance().also {
					it.context = context.applicationContext
					it.selectedDelivery = deliveryUnit
					it.selectedMeasurement = measureUnit
					it.selectedTemp = tempUnit
					it.selectedTds = tdsUnit
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
				val processor = exportProcessor.newInstance().also {
					it.context = context.applicationContext
					it.selectedDelivery = deliveryUnit
					it.selectedMeasurement = measureUnit
					it.selectedTemp = tempUnit
					it.selectedTds = tdsUnit
				}
				processor.beginDocument()

				// temp folder to write to
				val zipPathPrefix = garden?.let { plant.name.replace("[^a-zA-Z0-9]+".toRegex(), "-") + "/"} ?: ""

				// do processor stuff
				processor.printPlantDetails(plant)
				processor.printPlantStages(plant)
				processor.printPlantStats(plant)
				processor.printPlantActions(plant)

				val imagePaths = sortedMapOf<String, ArrayList<String>>()
				val stages = plant.getStages()
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

						val totalDays = TimeHelper.toDays(Math.abs(fileDate - plant.plantDate)).toInt()
						var stageChange: StageChange? = null
						with (stages.keys.iterator())
						{
							while (this.hasNext())
							{
								val k = next()
								stages[k]?.let { action ->
									if (action.date <= fileDate && stageChange == null)
									{
										stageChange = action as? StageChange
									}
								}
							}
						}

						var dateSuffix = ""
						stageChange?.let { change ->
							dateSuffix = " ${totalDays}/" + TimeHelper.toDays(fileDate - change.date).toInt() + context.getString(change.newStage.printString)[0].toLowerCase()
						}

						imagePaths.getOrPut(dateFolder(context, fileDate) + dateSuffix) { arrayListOf() }.add(zipPathPrefix + "images/" + dateFolder(context, fileDate) + "/" + fileDate + ".jpg")
					}
					catch (e: java.lang.Exception)
					{
						e.printStackTrace()
					}
				}
				processor.printPlantImages(imagePaths)

				// do chart stuff
				val viewModel = StatisticsFragment2.StatisticsViewModel(tdsUnit, deliveryUnit, measureUnit, tempUnit, plant)
				val totalWater = plant.actions?.sumBy { if (it is Water) 1 else 0 } ?: 0
				val width = ((1024 + (viewModel.totalDays * 20)) * 1).toInt()
				val height = (512 * 1).toInt()

				saveStagesChart(width, height, viewModel, zipPathPrefix, outFile)
				saveTempChart(width, height, viewModel, zipPathPrefix, outFile)
				saveTdsCharts(width, height, viewModel, zipPathPrefix, outFile)
				saveInputPhChart(width, height, viewModel, zipPathPrefix, outFile)
				saveAdditiveChart(width, height, viewModel, zipPathPrefix, outFile)

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
				temp.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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
				chart.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

	private fun saveStagesChart(width: Int, height: Int, viewModel: StatisticsFragment2.StatisticsViewModel, pathPrefix: String, outZip: ZipFile)
	{
		try
		{
			val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
			val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

			val stagesChart = HorizontalBarChart(context)

			with (stagesChart) {
				setExtraOffsets(30f, 30f, 30f, 30f)
				setPadding(100, 100, 100, 100)
				layoutParams = ViewGroup.LayoutParams(width, height)
				minimumWidth = width
				minimumHeight = height
				measure(widthMeasureSpec, heightMeasureSpec)
				requestLayout()
				layout(0, 0, width, height)

				// stage chart
				val labels = arrayOfNulls<String>(viewModel.plantStages.size)
				val yVals = FloatArray(viewModel.plantStages.size)

				var index = viewModel.plantStages.size - 1
				for (plantStage in viewModel.plantStages.keys)
				{
					yVals[index] = max(TimeHelper.toDays(viewModel.plantStages[plantStage] ?: 0).toFloat(), 1f)
					labels[index--] = context.getString(plantStage.printString)
				}

				val stageEntries = arrayListOf<BarEntry>()
				stageEntries += BarEntry(0f, yVals, viewModel.plantStages.keys.toList().asReversed())

				val stageData = BarDataSet(stageEntries, "")
				stageData.isHighlightEnabled = false
				stageData.stackLabels = labels
				stageData.colors = statsColours
				stageData.valueTypeface = Typeface.DEFAULT_BOLD
				stageData.valueTextSize = 10f
				stageData.valueFormatter = object : ValueFormatter()
				{
					override fun getBarStackedLabel(value: Float, stackedEntry: BarEntry?): String
					{
						stackedEntry?.let {
							(it.data as? List<PlantStage>)?.let { stages ->
								val stageIndex = it.yVals.indexOf(value)
								return "${value.toInt()}${context.getString(stages[stageIndex].printString)[0].toLowerCase()}"
							}
						}

						return super.getBarStackedLabel(value, stackedEntry)
					}
				}

				val barData = BarData(stageData)
				data = barData
				setDrawGridBackground(false)
				description = null
				isScaleYEnabled = false
				setDrawBorders(false)
				setDrawValueAboveBar(false)

				axisLeft.setDrawGridLines(false)
				axisLeft.axisMinimum = 0f
				axisLeft.textColor = 0xff000000.toInt()
				axisLeft.valueFormatter = object : ValueFormatter()
				{
					override fun getAxisLabel(value: Float, axis: AxisBase?): String
					{
						return "${value.toInt()}${context.getString(R.string.day_abbr)}"
					}
				}

				axisRight.setDrawLabels(false)
				axisRight.setDrawGridLines(false)

				xAxis.setDrawGridLines(false)
				xAxis.setDrawAxisLine(false)
				xAxis.setDrawLabels(false)

				legend.textColor = 0xff000000.toInt()
				legend.isWordWrapEnabled = true
			}

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = pathPrefix + "stages.jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				stagesChart.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

	private fun saveTempChart(width: Int, height: Int, viewModel: StatisticsFragment2.StatisticsViewModel, pathPrefix: String, outZip: ZipFile)
	{
		try
		{
			val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
			val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

			val temp_chart = LineChart(context)
			temp_chart.setExtraOffsets(30f, 30f, 30f, 30f)
			temp_chart.setPadding(100, 100, 100, 100)
			temp_chart.layoutParams = ViewGroup.LayoutParams(width, height)
			temp_chart.minimumWidth = width
			temp_chart.minimumHeight = height
			temp_chart.measure(widthMeasureSpec, heightMeasureSpec)
			temp_chart.requestLayout()
			temp_chart.layout(0, 0, width, height)

			with (temp_chart) {
				setVisibleYRangeMaximum(viewModel.tempStats.max?.toFloat() ?: 0.0f, com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT)
				style()
				legend.textColor = 0xff000000.toInt()
				axisLeft.textColor = 0xff000000.toInt()
				xAxis.textColor = 0xff000000.toInt()
				xAxis.labelCount = 25
				xAxis.textSize = 8.0f
				axisLeft.textSize = 8.0f

				axisLeft.valueFormatter = object : ValueFormatter()
				{
					override fun getAxisLabel(value: Float, axis: AxisBase?): String
					{
						return "${value.formatWhole()}°${viewModel.selectedTempUnit.label}"
					}
				}

				xAxis.valueFormatter = object : ValueFormatter()
				{
					override fun getAxisLabel(value: Float, axis: AxisBase?): String
					{
						return viewModel.waterDates.getOrNull(value.toInt())?.transform {
							"${total}/${day}${context.getString(stage.printString).toLowerCase()[0]}"
						} ?: ""
					}
				}
			}

			val sets = arrayListOf<ILineDataSet>()

			sets += LineDataSet(viewModel.tempValues, context.getString(R.string.stat_input_ph)).apply {
				color = statsColours[0]
				fillColor = color
				setCircleColor(color)
				styleDataset(context!!, this, color)
			}

			sets += LineDataSet(viewModel.tempValues.rollingAverage(), context.getString(R.string.stat_average_temp)).apply {
				color = ColorUtils.blendARGB(statsColours[0], 0xffffffff.toInt(), 0.4f)
				setDrawCircles(false)
				setDrawValues(false)
				setDrawCircleHole(false)
				setDrawHighlightIndicators(true)
				cubicIntensity = 1f
				lineWidth = 2.0f
				isHighlightEnabled = false
			}

			temp_chart.data = LineData(sets)
			temp_chart.data.setDrawValues(true)

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = pathPrefix + "temp.jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				temp_chart.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

	private fun saveTdsCharts(width: Int, height: Int, viewModel: StatisticsFragment2.StatisticsViewModel, pathPrefix: String, outZip: ZipFile)
	{
		val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
		val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

		viewModel.tdsValues.forEach { (name, values) ->
			val tds = LineChart(context)
			tds.setExtraOffsets(30f, 30f, 30f, 30f)
			tds.setPadding(100, 100, 100, 100)
			tds.layoutParams = ViewGroup.LayoutParams(width, height)
			tds.minimumWidth = width
			tds.minimumHeight = height
			tds.measure(widthMeasureSpec, heightMeasureSpec)
			tds.requestLayout()
			tds.layout(0, 0, width, height)

			with (tds) {
				style()
				legend.textColor = 0xff000000.toInt()
				axisLeft.textColor = 0xff000000.toInt()
				xAxis.textColor = 0xff000000.toInt()
				xAxis.labelCount = 25
				xAxis.textSize = 8.0f
				axisLeft.textSize = 8.0f

				xAxis.valueFormatter = object : ValueFormatter()
				{
					override fun getAxisLabel(value: Float, axis: AxisBase?): String
					{
						return viewModel.waterDates.getOrNull(value.toInt())?.transform {
							"${total}/${day}${context.getString(stage.printString).toLowerCase()[0]}"
						} ?: ""
					}
				}

				val sets = arrayListOf<ILineDataSet>()
				sets += LineDataSet(values, context.getString(name.strRes)).apply {
					color = statsColours[viewModel.tdsValues.keys.indexOfFirst { it == name }.absoluteValue % statsColours.size]
					fillColor = color
					setCircleColor(color)
					styleDataset(context!!, this, color)
				}
				sets += LineDataSet(values.rollingAverage(), context.getString(R.string.stat_average_tds, name.label)).apply {
					color = ColorUtils.blendARGB(statsColours[viewModel.tdsValues.keys.indexOfFirst { it == name }.absoluteValue % statsColours.size], 0xffffffff.toInt(), 0.4f)
					setDrawCircles(false)
					setDrawValues(false)
					setDrawCircleHole(false)
					setDrawHighlightIndicators(true)
					cubicIntensity = 1f
					lineWidth = 2.0f
					isHighlightEnabled = false
				}

				data = LineData(sets)
				data.setDrawValues(true)
			}

			try
			{
				val parameters = ZipParameters()
				parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
				parameters.fileNameInZip = pathPrefix + name.enStr + ".jpg"
				parameters.isSourceExternalStream = true

				val outputStream = ByteArrayOutputStream()
				tds.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

	private fun saveInputPhChart(width: Int, height: Int, viewModel: StatisticsFragment2.StatisticsViewModel, pathPrefix: String, outZip: ZipFile)
	{
		try
		{
			fun saveChart(sets: ArrayList<ILineDataSet>, name: String)
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

				with (inputPh) {
					setVisibleYRangeMaximum(max(viewModel.phStats.max?.toFloat() ?: 0.0f, viewModel.runoffStats.max?.toFloat() ?: 0.0f), com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT)
					style()
					legend.textColor = 0xff000000.toInt()
					axisLeft.textColor = 0xff000000.toInt()
					xAxis.textColor = 0xff000000.toInt()
					xAxis.labelCount = 25
					xAxis.textSize = 8.0f
					axisLeft.textSize = 8.0f

					xAxis.valueFormatter = object : ValueFormatter()
					{
						override fun getAxisLabel(value: Float, axis: AxisBase?): String
						{
							return viewModel.waterDates.getOrNull(value.toInt())?.transform {
								"${total}/${day}${context.getString(stage.printString).toLowerCase()[0]}"
							} ?: ""
						}
					}
				}
				inputPh.data = LineData(sets)
				inputPh.data.setDrawValues(true)

				try
				{
					val parameters = ZipParameters()
					parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
					parameters.fileNameInZip = "$pathPrefix$name.jpg"
					parameters.isSourceExternalStream = true

					val outputStream = ByteArrayOutputStream()
					inputPh.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
					val stream = ByteArrayInputStream(outputStream.toByteArray())
					outZip.addStream(stream, parameters)

					stream.close()
				}
				catch (e: Exception)
				{
					e.printStackTrace()
				}
			}

			val sets = arrayListOf<ILineDataSet>()
			sets += LineDataSet(viewModel.phValues, context.getString(R.string.stat_input_ph)).apply {
				color = statsColours[0]
				fillColor = color
				setCircleColor(color)
				styleDataset(context!!, this, color)
			}
			sets += LineDataSet(viewModel.phValues.rollingAverage(), context.getString(R.string.stat_average_ph)).apply {
				color = ColorUtils.blendARGB(statsColours[0], 0xffffffff.toInt(), 0.4f)
				setDrawCircles(false)
				setDrawValues(false)
				setDrawCircleHole(false)
				setDrawHighlightIndicators(true)
				cubicIntensity = 1f
				lineWidth = 2.0f
				isHighlightEnabled = false
			}
			saveChart(sets, "input-ph")
			sets.clear()
			sets += LineDataSet(viewModel.runoffValues, context.getString(R.string.stat_runoff_ph)).apply {
				color = statsColours[1]
				fillColor = color
				setCircleColor(color)
				styleDataset(context!!, this, color)
			}
			sets += LineDataSet(viewModel.runoffValues.rollingAverage(), context.getString(R.string.stat_average_runoff_ph)).apply {
				color = ColorUtils.blendARGB(statsColours[1], 0xffffffff.toInt(), 0.4f)
				setDrawCircles(false)
				setDrawValues(false)
				setDrawCircleHole(false)
				setDrawHighlightIndicators(true)
				cubicIntensity = 1f
				lineWidth = 2.0f
				isHighlightEnabled = false
			}
			saveChart(sets, "runoff-ph")
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	private fun saveAdditiveChart(width: Int, height: Int, viewModel: StatisticsFragment2.StatisticsViewModel, pathPrefix: String, outZip: ZipFile)
	{
		val entries = arrayListOf<LegendEntry>()
		viewModel.additiveValues.toSortedMap().let {
			var index = 0
			it.forEach { (k, v) ->
				entries.add(LegendEntry().apply {
						label = k
						formColor = statsColours[index]
					})

				index++
				if (index >= statsColours.size) index = 0
			}
		}

		fun displayConcentrationChart()
		{
			val dataSets = arrayListOf<ILineDataSet>()
			var index = 0
			viewModel.additiveValues.toSortedMap().let {
				it.forEach { (k, v) ->
					dataSets += LineDataSet(v, k).apply {
						color = statsColours[index]
						fillColor = color
						setCircleColor(color)
						styleDataset(context!!, this, color)
					}

					index++
					if (index >= statsColours.size) index = 0
				}
			}

			fun saveChart(lineData: LineData, fileName: String)
			{
				try
				{
					val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
					val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

					val additives = LineChart(context)
					with (additives) {
						setExtraOffsets(30f, 30f, 30f, 30f)
						setPadding(100, 100, 100, 100)
						layoutParams = ViewGroup.LayoutParams(width, height)
						minimumWidth = width
						minimumHeight = height
						measure(widthMeasureSpec, heightMeasureSpec)
						requestLayout()
						layout(0, 0, width, height)
						style()
						legend.textColor = 0xff000000.toInt()
						axisLeft.textColor = 0xff000000.toInt()
						xAxis.textColor = 0xff000000.toInt()
						xAxis.labelCount = 25
						xAxis.textSize = 8.0f
						axisLeft.textSize = 8.0f

						axisLeft.granularity = 1f
						axisLeft.valueFormatter = object : ValueFormatter()
						{
							override fun getAxisLabel(value: Float, axis: AxisBase?): String
							{
								return "${value.formatWhole()}${viewModel.selectedMeasurementUnit.label}/${viewModel.selectedDeliveryUnit.label}"
							}
						}

						xAxis.valueFormatter = object : ValueFormatter()
						{
							override fun getAxisLabel(value: Float, axis: AxisBase?): String
							{
								return viewModel.waterDates.getOrNull(value.toInt())?.transform {
									"${total}/${day}${context.getString(stage.printString).toLowerCase()[0]}"
								} ?: ""
							}
						}

						legend.isWordWrapEnabled = true
						legend.setCustom(entries)
						data = lineData
						data.setDrawValues(true)
					}

					try
					{
						val parameters = ZipParameters()
						parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
						parameters.fileNameInZip = "$pathPrefix$fileName.jpg"
						parameters.isSourceExternalStream = true

						val outputStream = ByteArrayOutputStream()
						additives.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

			saveChart(LineData(dataSets), "additives")

			dataSets.forEach { set ->
				saveChart(LineData(set), set.label.normalise())
			}
		}

		fun displayTotalsChart()
		{
			val pieData = arrayListOf<PieEntry>()
			val colors = arrayListOf<Int>()
			viewModel.additiveTotalValues.toSortedMap().let { values ->
				var index = 0

				values.forEach { (k, v) ->
					var total = 0.0
					v.forEach { entry ->
						total += entry.y
					}

					pieData += PieEntry(total.toFloat()).apply {
						colors += statsColours[index]
					}

					index++
					if (index >= statsColours.size) index = 0
				}
			}

			try
			{
				val width = 1024
				val height = 1024
				val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
				val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

				val additives = PieChart(context)
				with (additives) {
					layoutParams = ViewGroup.LayoutParams(width, height)
					minimumWidth = width
					minimumHeight = width
					measure(widthMeasureSpec, heightMeasureSpec)
					requestLayout()
					layout(0, 0, width, width)

					description = null
					setHoleColor(0x00ffffff)
					legend.setCustom(entries)
					legend.form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
					legend.textColor = 0xff000000.toInt()
					legend.isWordWrapEnabled = true

					data = PieData(PieDataSet(pieData, "").apply {
						this.colors = colors
						this.valueTextSize = 12f
						this.valueFormatter = object : ValueFormatter()
						{
							override fun getFormattedValue(value: Float): String
							{
								return "${value.formatWhole()}${viewModel.selectedMeasurementUnit.label}"
							}
						}
					})
				}

				try
				{
					val parameters = ZipParameters()
					parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
					parameters.fileNameInZip = pathPrefix + "total-additives.jpg"
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

		fun displayOvertimeChart()
		{
			val barSets = arrayListOf<IBarDataSet>()
			val dataSets = arrayListOf<ILineDataSet>()
			var index = 0
			val newValues = sortedMapOf<String, ArrayList<Entry>>()

			viewModel.additiveTotalValues.toSortedMap().let {
				it.forEach { (key, entries) ->
					val newEntries = arrayListOf<Entry>()
					var lastEntry: Entry? = null
					entries.forEach { entry ->
						val newEntry = Entry(entry.x, entry.y + (lastEntry?.y ?: 0.0f))
						newEntries.add(newEntry)
						lastEntry = newEntry
					}

					newValues[key] = newEntries

					dataSets += LineDataSet(newValues[key], key).apply {
						color = statsColours[index]
						fillColor = color
						setCircleColor(color)
						styleDataset(context!!, this, color)
					}

					index++
					if (index >= statsColours.size) index = 0
				}
			}

			val lineData = LineData(dataSets)
			try
			{
				val height = height * 2
				val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
				val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

				val additives = LineChart(context)
				with (additives) {
					setExtraOffsets(30f, 30f, 30f, 30f)
					setPadding(100, 100, 100, 100)
					layoutParams = ViewGroup.LayoutParams(width, height)
					minimumWidth = width
					minimumHeight = height
					measure(widthMeasureSpec, heightMeasureSpec)
					requestLayout()
					layout(0, 0, width, height)
					style()
					legend.textColor = 0xff000000.toInt()
					axisLeft.textColor = 0xff000000.toInt()
					xAxis.textColor = 0xff000000.toInt()
					axisLeft.labelCount = 25
					xAxis.labelCount = 25
					xAxis.textSize = 8.0f
					axisLeft.textSize = 8.0f
					axisLeft.axisMinimum = 0.0f

					axisLeft.granularity = 1f
					axisLeft.valueFormatter = object : ValueFormatter()
					{
						override fun getAxisLabel(value: Float, axis: AxisBase?): String
						{
							return "${value.formatWhole()}${viewModel.selectedMeasurementUnit.label}/${viewModel.selectedDeliveryUnit.label}"
						}
					}

					xAxis.valueFormatter = object : ValueFormatter()
					{
						override fun getAxisLabel(value: Float, axis: AxisBase?): String
						{
							return viewModel.waterDates.getOrNull(value.toInt())?.transform {
								"${total}/${day}${context.getString(stage.printString).toLowerCase()[0]}"
							} ?: ""
						}
					}

					legend.setCustom(entries)
					legend.isWordWrapEnabled = true
					legend.yOffset = 10f
					legend.xOffset = 10f
					data = lineData
					data.setDrawValues(true)
				}

				try
				{
					val parameters = ZipParameters()
					parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
					parameters.fileNameInZip = pathPrefix + "additives-over-time.jpg"
					parameters.isSourceExternalStream = true

					val outputStream = ByteArrayOutputStream()
					additives.scaledBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

		displayConcentrationChart()
		displayTotalsChart()
		displayOvertimeChart()
	}

	private fun styleDataset(context: Context, data: LineDataSet, colour: Int)
	{
		val context = ContextThemeWrapper(context, R.style.AppTheme)
		data.valueTextColor = R.attr.colorAccent.resolveColor(context)
		data.setCircleColor(R.attr.colorAccent.resolveColor(context))
		data.cubicIntensity = 0.2f
		data.lineWidth = 3.0f
		data.setDrawCircleHole(true)
		data.color = colour
		data.setCircleColor(colour)
		data.circleRadius = 4.0f
		data.setDrawHighlightIndicators(true)
		data.isHighlightEnabled = true
		data.highlightLineWidth = 2f
		data.highLightColor = ColorUtils.setAlphaComponent(colour, 96)
		data.setDrawValues(false)
		data.valueFormatter = StatsHelper.formatter
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
			return SimpleDateFormat("yyyy-MM-dd").format(Date(timestamp)).replace("[^0-9]".toRegex(), "-")
		}
	}
}
