package me.anon.grow3.util.adapter

import com.squareup.moshi.*
import me.anon.grow3.data.model.*
import me.anon.grow3.util.MoshiHelper
import java.io.IOException

public class LogJsonAdapter : JsonAdapter<Log>()
{
	private val moshi: Moshi by lazy { MoshiHelper.addAdapters(Moshi.Builder()).build() }
	private val environmentAdapter = EnvironmentJsonAdapter(moshi)
	private val harvestAdapter = HarvestJsonAdapter(moshi)
	private val maintenanceAdapter = MaintenanceJsonAdapter(moshi)
	private val pesticideAdapter = PesticideJsonAdapter(moshi)
	private val photoAdapter = PhotoJsonAdapter(moshi)
	private val stageChangeAdapter = StageChangeJsonAdapter(moshi)
	private val waterAdapter = WaterJsonAdapter(moshi)

	@Throws(IOException::class)
	override fun fromJson(reader: JsonReader): Log
	{
		var value: Log? = null

		var type = ""
		val temp = reader.peekJson()
		temp.beginObject()
		while (temp.hasNext())
		{
			when (temp.selectName(JsonReader.Options.of("action")))
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
			"Environment" -> value = environmentAdapter.fromJson(reader)
			"Harvest" -> value = harvestAdapter.fromJson(reader)
			"Maintenance" -> value = maintenanceAdapter.fromJson(reader)
			"Pesticide" -> value = pesticideAdapter.fromJson(reader)
			"Photo" -> value = photoAdapter.fromJson(reader)
			"StageChange" -> value = stageChangeAdapter.fromJson(reader)
			"Water" -> value = waterAdapter.fromJson(reader)
			else -> throw JsonDataException("Log $type not recognised")
		}

		return value
	}

	@Throws(IOException::class)
	override fun toJson(writer: JsonWriter, value: Log?)
	{
		when (value)
		{
			is Environment -> environmentAdapter.toJson(writer, value)
			is Harvest -> harvestAdapter.toJson(writer, value)
			is Maintenance -> maintenanceAdapter.toJson(writer, value)
			is Pesticide -> pesticideAdapter.toJson(writer, value)
			is Photo -> photoAdapter.toJson(writer, value)
			is StageChange -> stageChangeAdapter.toJson(writer, value)
			is Water -> waterAdapter.toJson(writer, value)
		}
	}
}
