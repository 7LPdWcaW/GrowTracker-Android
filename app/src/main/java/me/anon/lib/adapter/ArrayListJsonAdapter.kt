package me.anon.lib.adapter

import com.squareup.moshi.*
import com.squareup.moshi.JsonAdapter.Factory
import java.io.IOException
import java.lang.reflect.Type
import java.util.*

/**
 * Json adapter class for properly converting items for array list
 */
public class ArrayListJsonAdapter<T>(
	var elementAdapter: JsonAdapter<T>
) : JsonAdapter<ArrayList<T>>()
{
	companion object
	{
		val FACTORY: JsonAdapter.Factory = Factory { type, annotations, moshi ->
			val rawType = Types.getRawType(type)

			return@Factory when
			{
				!annotations.isEmpty() -> null
				rawType == ArrayList::class.java -> newArrayListAdapter<Any>(type, moshi).nullSafe()
				else -> null
			}
		}

		@JvmStatic
		public fun <T> newArrayListAdapter(type: Type, moshi: Moshi): JsonAdapter<ArrayList<T>>
		{
			val elementType = Types.collectionElementType(type, ArrayList::class.java)
			val elementAdapter = moshi.adapter<T>(elementType)

			return ArrayListJsonAdapter<T>(elementAdapter)
		}
	}

	@Throws(IOException::class)
	override fun fromJson(reader: JsonReader): ArrayList<T>
	{
		val result = arrayListOf<T>()
		reader.beginArray()
		while (reader.hasNext())
		{
			elementAdapter.fromJson(reader)?.let {
				result.add(it)
			}
		}

		reader.endArray()
		return result
	}

	@Throws(IOException::class)
	override fun toJson(writer: JsonWriter, value: ArrayList<T>?)
	{
		writer.beginArray()
		for (element in value!!)
		{
			elementAdapter.toJson(writer, element)
		}

		writer.endArray()
	}
}
