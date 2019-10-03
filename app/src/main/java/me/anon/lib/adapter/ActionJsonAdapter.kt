package me.anon.lib.adapter

import com.squareup.moshi.*
import me.anon.lib.helper.MoshiHelper
import me.anon.model.*
import java.io.IOException

public class ActionJsonAdapter : JsonAdapter<Action>()
{
	private val moshi: Moshi by lazy { MoshiHelper.addAdapters(Moshi.Builder()).build() }
	private val emptyActionAdater = EmptyActionJsonAdapter(moshi)
	private val noteActionAdapter = NoteActionJsonAdapter(moshi)
	private val stageChangeAdapter = StageChangeJsonAdapter(moshi)
	private val temperatureChangeAdapter = TemperatureChangeJsonAdapter(moshi)
	private val waterAdapter = WaterJsonAdapter(moshi)

	@Throws(IOException::class)
	override fun fromJson(reader: JsonReader): Action
	{
		var value: Action? = null

		var type = ""
		val temp = reader.peekJson()
		temp.beginObject()
		while (temp.hasNext())
		{
			when (temp.selectName(JsonReader.Options.of("type")))
			{
				0 -> {
					type = temp.nextString()
				}
				-1 -> {
					temp.skipName()
					temp.skipValue()
				}
			}
		}

		when (type)
		{
			"Feed", "Water" -> value = waterAdapter.fromJson(reader)
			"Action" -> value = emptyActionAdater.fromJson(reader)
			"Note" -> value = noteActionAdapter.fromJson(reader)
			"StageChange" -> value = stageChangeAdapter.fromJson(reader)
			"TemperatureChange" -> value = temperatureChangeAdapter.fromJson(reader)
			else -> throw JsonDataException("Action $type not recognised")
		}

		return value
	}

	@Throws(IOException::class)
	override fun toJson(writer: JsonWriter, value: Action?)
	{
		when (value)
		{
			is Water -> waterAdapter.toJson(writer, value)
			is EmptyAction -> emptyActionAdater.toJson(writer, EmptyAction(value.action, value.date, value.notes))
			is NoteAction -> noteActionAdapter.toJson(writer, value)
			is StageChange -> stageChangeAdapter.toJson(writer, value)
			is TemperatureChange -> temperatureChangeAdapter.toJson(writer, value)
		}
	}
}
