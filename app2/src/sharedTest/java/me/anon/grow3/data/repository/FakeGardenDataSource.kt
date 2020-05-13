package me.anon.grow3.data.repository

import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.source.GardensDataSource

/**
 * // TODO: Add class description
 */
public class FakeGardenDataSource(private val diaries: MutableList<Diary>) : GardensDataSource
{
	override suspend fun addGarden(diary: Diary): List<Diary> = diaries.apply {
		add(diary)
	}

	override suspend fun getGardenById(gardenId: String): Diary? = diaries.find { it.id == gardenId }

	override suspend fun getGardens(): List<Diary> = diaries

	override suspend fun sync(direction: GardensDataSource.SyncDirection): List<Diary> = diaries
}
