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
		TODO("Not yet implemented")
	}

	override suspend fun retrieveLog(id: String): Log
		= tempCache.find { it is Log && it.id == id } as Log

	override suspend fun cache(crop: Crop): String
	{
		tempCache.add(crop)
		return crop.id
	}

	override suspend fun retrieveCrop(id: String): Crop
	{
		TODO("Not yet implemented")
	}

	override suspend fun cache(diary: Diary): String
	{
		TODO("Not yet implemented")
	}

	override suspend fun retrieveDiary(id: String): Diary
	{
		TODO("Not yet implemented")
	}

	override suspend fun cache(map: Map<String, Any?>): String
	{
		TODO("Not yet implemented")
	}

	override suspend fun retrieveMap(id: String): Map<String, Any?>
	{
		TODO("Not yet implemented")
	}
}
