package me.anon.grow3.data.source

import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Log

interface CacheDataSource
{
	public suspend fun cache(log: Log): String
	public suspend fun retrieveLog(id: String): Log
	public fun clearLog()

	public suspend fun cache(crop: Crop): String
	public suspend fun retrieveCrop(id: String): Crop
	public fun clearCrop()

	public suspend fun cache(map: Map<String, Any?>): String
	public suspend fun retrieveMap(id: String): Map<String, Any?>
	public fun clearMap()
}
