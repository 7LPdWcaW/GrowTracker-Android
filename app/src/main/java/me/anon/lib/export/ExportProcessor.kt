package me.anon.lib.export

import android.content.Context
import me.anon.lib.TdsUnit
import me.anon.lib.TempUnit
import me.anon.lib.Unit
import me.anon.model.Garden
import me.anon.model.Plant
import net.lingala.zip4j.core.ZipFile
import java.util.*

/**
 * Defines methods to interact with when processing the export data
 */
open abstract class ExportProcessor
{
	public var context: Context? = null
	public var selectedTds: TdsUnit? = null
	public var selectedMeasurement: Unit? = null
	public var selectedDelivery: Unit? = null
	public var selectedTemp: TempUnit? = null

	open abstract fun beginDocument(isPlant: Boolean = true)
	open abstract fun endDocument(zipFile: ZipFile, zipPathPrefix: String = "")

	open abstract fun printPlantDetails(plant: Plant)
	open abstract fun printPlantStages(plant: Plant)
	open abstract fun printPlantStats(plant: Plant)
	open abstract fun printPlantActions(plant: Plant)
	open abstract fun printPlantImages(map: SortedMap<String, ArrayList<String>>)

	open abstract fun printGardenDetails(garden: Garden)
	open abstract fun printGardenStats(garden: Garden)
	open abstract fun printGardenActions(garden: Garden)

	open abstract fun printRaw(plant: Plant)
}
