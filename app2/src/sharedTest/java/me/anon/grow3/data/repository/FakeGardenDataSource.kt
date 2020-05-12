package me.anon.grow3.data.repository

import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.source.GardensDataSource

/**
 * // TODO: Add class description
 */
public class FakeGardenDataSource(private val gardens: MutableList<Garden>) : GardensDataSource
{
	override suspend fun addGarden(garden: Garden): List<Garden> = gardens.apply {
		add(garden)
	}

	override suspend fun getGardenById(gardenId: String): Garden? = gardens.find { it.id == gardenId }

	override suspend fun getGardens(): List<Garden> = gardens

	override suspend fun sync(direction: GardensDataSource.SyncDirection): List<Garden> = gardens
}
