package me.anon.lib.helper

import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.anon.lib.adapter.ActionJsonAdapter
import me.anon.lib.adapter.ArrayListJsonAdapter
import me.anon.model.Action
import okio.Okio
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

object MoshiHelper
{
	@JvmStatic
	public fun <T> parse(json: String, type: Type): T
	{
		return getMoshi().adapter<T>(type).fromJson(json) as T
	}

	@JvmStatic
	public fun <T> parse(json: InputStream, type: Type): T
	{
		return getMoshi().adapter<T>(type).fromJson(Okio.buffer(Okio.source(json))) as T
	}

	@JvmStatic
	public fun toJson(obj: Any): String
	{
		return getMoshi().adapter(obj.javaClass).toJson(obj)
	}

	@JvmStatic
	public fun <T> toJson(obj: T, type: Type): String
	{
		return getMoshi().adapter<T>(type).toJson(obj)
	}

	@JvmStatic
	public fun toJson(obj: Any, outputStream: OutputStream)
	{
		getMoshi().adapter(obj.javaClass).toJson(JsonWriter.of(Okio.buffer(Okio.sink(outputStream))), obj)
	}

	@JvmStatic
	public fun <T> toJson(obj: T, type: Type, outputStream: OutputStream)
	{
		getMoshi().adapter<T>(type).toJson(JsonWriter.of(Okio.buffer(Okio.sink(outputStream))), obj)
	}

	public fun getMoshi(): Moshi
	{
		val moshi = Moshi.Builder()
		moshi.add<Action>(Action::class.java, ActionJsonAdapter())
		moshi.add(ArrayListJsonAdapter.FACTORY)
		moshi.add(KotlinJsonAdapterFactory())

		return moshi.build()
	}

	public fun addAdapters(builder: Moshi.Builder): Moshi.Builder
	{
		builder.add<Action>(Action::class.java, ActionJsonAdapter())
		builder.add(ArrayListJsonAdapter.FACTORY)
		builder.add(KotlinJsonAdapterFactory())
		return builder
	}
}
