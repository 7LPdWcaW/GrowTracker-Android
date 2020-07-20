package me.anon.grow3.data.source

import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log

interface CacheDataSource
{
	public suspend fun cache(log: Log): String
	public suspend fun retrieveLog(id: String): Log

	public suspend fun cache(crop: Crop): String
	public suspend fun retrieveCrop(id: String): Crop

	public suspend fun cache(diary: Diary): String
	public suspend fun retrieveDiary(id: String): Diary

	public suspend fun cache(map: Map<String, Any?>): String
	public suspend fun retrieveMap(id: String): Map<String, Any?>
}
