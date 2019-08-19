package me.anon.lib.helper

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type

object MoshiHelper
{
	public fun <T> parse(json: String, type: Type): T
	{
		return null as T
	}

	public fun getMoshi(): Moshi
	{
		val moshi = Moshi.Builder()
		moshi.add(KotlinJsonAdapterFactory())

		return moshi.build()
	}
}
