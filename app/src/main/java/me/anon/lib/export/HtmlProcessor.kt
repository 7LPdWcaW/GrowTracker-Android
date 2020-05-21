package me.anon.lib.export

import me.anon.model.Garden
import me.anon.model.Plant
import net.lingala.zip4j.core.ZipFile
import java.util.*

/**
 * // TODO: Add class description
 */
class HtmlProcessor : ExportProcessor()
{
	override fun beginDocument(isPlant: Boolean)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun endDocument(zipFile: ZipFile, ziPFilePath: String)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printPlantDetails(plant: Plant)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printPlantStages(plant: Plant)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printPlantStats(plant: Plant)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printPlantActions(plant: Plant)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printPlantImages(map: SortedMap<String, ArrayList<String>>)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printGardenDetails(garden: Garden)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printGardenStats(garden: Garden)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printGardenActions(garden: Garden)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun printRaw(plant: Plant)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}
