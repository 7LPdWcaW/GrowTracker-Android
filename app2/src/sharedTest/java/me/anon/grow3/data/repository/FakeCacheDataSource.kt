package me.anon.grow3.data.repository

import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.source.CacheDataSource

public class FakeCacheDataSource() : CacheDataSource
{
	private var tempCache = arrayListOf<Any>()

	override suspend fun cache(log: Log): String
	{
		tempCache.add(log)
		return log.id
	}

	override suspend fun retrieveLog(id: String): Log
		= tempCache.find { it is Log && it.id == id } as Log

	override suspend fun cache(crop: Crop): String
	{
		tempCache.add(crop)
		return crop.id
	}

	override suspend fun retrieveCrop(id: String): Crop
		= tempCache.find { it is Crop && it.id == id } as Crop

	override suspend fun cache(diary: Diary): String
	{
		tempCache.add(diary)
		return diary.id
	}

	override suspend fun retrieveDiary(id: String): Diary
		= tempCache.find { it is Diary && it.id == id } as Diary

	override suspend fun cache(map: Map<String, Any?>): String
	{
		tempCache.add(map)
		return (tempCache.size - 1).toString()
	}

	override suspend fun retrieveMap(id: String): Map<String, Any?>
		= tempCache[id.toInt()] as Map<String, Any?>
}
