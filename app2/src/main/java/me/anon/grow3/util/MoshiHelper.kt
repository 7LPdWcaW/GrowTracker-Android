package me.anon.grow3.util

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.anon.grow3.data.model.Log
import me.anon.grow3.util.adapter.ArrayListJsonAdapter
import me.anon.grow3.util.adapter.LogJsonAdapter
import okio.Okio
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

object MoshiHelper
{
	public fun <T> parse(json: String, type: Type): T = moshi.adapter<T>(type).fromJson(json) as T
	public fun <T> parse(json: File, type: Type): T = moshi.adapter<T>(type).fromJson(JsonReader.of(Okio.buffer(Okio.source(json)))) as T
	public fun <T> parse(json: InputStream, type: Type): T = moshi.adapter<T>(type).fromJson(JsonReader.of(Okio.buffer(Okio.source(json)))) as T

	public fun toJson(obj: Any): String = moshi.adapter(obj.javaClass).toJson(obj)
	public fun <T> toJson(obj: T, type: Type): String = moshi.adapter<T>(type).toJson(obj)
	public fun <T> toJson(obj: T, type: Type, outputStream: OutputStream)
	{
		val buffer = Okio.buffer(Okio.sink(outputStream))
		moshi.adapter<T>(type).toJson(buffer, obj)
		buffer.flush()
	}

	public val moshi: Moshi by lazy {
		val moshi = Moshi.Builder()
		moshi.add<Log>(Log::class.java, LogJsonAdapter())
		addAdapters(moshi)

		moshi.build()
	}

	public fun addAdapters(builder: Moshi.Builder): Moshi.Builder
	{
		builder.add(ArrayListJsonAdapter.FACTORY)
		builder.add(KotlinJsonAdapterFactory())
		return builder
	}
}
