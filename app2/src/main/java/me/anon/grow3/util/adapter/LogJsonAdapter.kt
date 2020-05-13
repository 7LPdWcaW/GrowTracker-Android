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

		return when (type)
		{
			"Environment" -> environmentAdapter.fromJson(reader)
			"Harvest" -> harvestAdapter.fromJson(reader)
			"Maintenance" -> maintenanceAdapter.fromJson(reader)
			"Pesticide" -> pesticideAdapter.fromJson(reader)
			"Photo" -> photoAdapter.fromJson(reader)
			"StageChange" -> stageChangeAdapter.fromJson(reader)
			"Water" -> waterAdapter.fromJson(reader)
			else -> throw JsonDataException("Log $type not recognised")
		}
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
