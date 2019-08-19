package me.anon.lib.adapter

import com.squareup.moshi.*
import me.anon.lib.helper.MoshiHelper
import me.anon.model.*
import java.io.IOException

public class ActionJsonAdapter() : JsonAdapter<Action>()
{
	private val moshi: Moshi by lazy { MoshiHelper.addAdapters(Moshi.Builder()).build() }
	private val emptyActionAdater: JsonAdapter<EmptyAction> by lazy { moshi.adapter<EmptyAction>(EmptyAction::class.java) }
	private val noteActionAdapter: JsonAdapter<NoteAction> by lazy { moshi.adapter<NoteAction>(NoteAction::class.java) }
	private val stageChangeAdapter: JsonAdapter<StageChange> by lazy { moshi.adapter<StageChange>(StageChange::class.java) }
	private val waterAdapter: JsonAdapter<Water> by lazy { moshi.adapter<Water>(Water::class.java) }

	@Throws(IOException::class)
	override fun fromJson(reader: JsonReader): Action
	{
		var value: Action? = null
		if (reader.hasNext())
		{
			val temp = reader.peekJson()
			while (temp.hasNext())
			{
				when (temp.selectName(JsonReader.Options.of("type")))
				{
					0 -> {
						val type = temp.nextString()
						when (type)
						{
							"Feed", "Water" -> value = waterAdapter.fromJsonValue(reader)
							"Action" -> value = emptyActionAdater.fromJsonValue(reader)
							"Note" -> value = noteActionAdapter.fromJsonValue(reader)
							"StageChange" -> value = stageChangeAdapter.fromJsonValue(reader)
							else -> throw JsonDataException("Action $type not recognised")
						}
					}
				}
			}
		}

		return value!!
	}

	@Throws(IOException::class)
	override fun toJson(writer: JsonWriter, value: Action?)
	{
		writer.beginObject()
		writer.endObject()
	}
}
