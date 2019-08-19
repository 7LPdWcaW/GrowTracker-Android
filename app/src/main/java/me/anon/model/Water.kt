package me.anon.model

import android.content.Context
import android.os.Parcelable
import android.preference.PreferenceManager
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import me.anon.grow.R
import me.anon.lib.TempUnit
import me.anon.lib.Unit

/**
 * // TODO: Add class description
 */
@Parcelize
@JsonClass(generateAdapter = true)
class Water(
	var ppm: Double? = null,
	var ph: Double? = null,
	var runoff: Double? = null,
	var amount: Double? = null,
	var temp: Double? = null,
	var additives: ArrayList<Additive> = arrayListOf(),

	override var date: Long = System.currentTimeMillis(),
	override var notes: String? = null
) : Action(date, notes), Parcelable
{
	@Deprecated("")
	public var nutrient: Nutrient? = null
	@Deprecated("")
	public var mlpl: Double? = null

	public fun getSummary(context: Context): String
	{
		val measureUnit = Unit.getSelectedMeasurementUnit(context)
		val deliveryUnit = Unit.getSelectedDeliveryUnit(context)
		val tempUnit = TempUnit.getSelectedTemperatureUnit(context)
		val usingEc = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("tds_ec", false)

		var summary = ""
		var waterStr = StringBuilder()

		ph?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_ph))
			waterStr.append("</b>")
			waterStr.append(it)
			waterStr.append(", ")
		}

		runoff?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_out_ph))
			waterStr.append("</b>")
			waterStr.append(it)
			waterStr.append(", ")
		}

		summary += if (waterStr.toString().isNotEmpty()) waterStr.toString().substring(0, waterStr.length - 2) + "<br/>" else ""

		waterStr = StringBuilder()

		ppm?.let {
			var ppm = it.toLong().toString()
			if (usingEc)
			{
				waterStr.append("<b>EC: </b>")
				ppm = (it * 2.0 / 1000.0).toString()
			}
			else
			{
				waterStr.append("<b>PPM: </b>")
			}

			waterStr.append(ppm)
			waterStr.append(", ")
		}

		amount?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_amount))
			waterStr.append("</b>")
			waterStr.append(Unit.ML.to(deliveryUnit, it))
			waterStr.append(deliveryUnit.label)
			waterStr.append(", ")
		}

		temp?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_temp))
			waterStr.append("</b>")
			waterStr.append(TempUnit.CELCIUS.to(tempUnit, it))
			waterStr.append("º").append(tempUnit.label).append(", ")
		}

		summary += if (waterStr.toString().isNotEmpty()) waterStr.toString().substring(0, waterStr.length - 2) + "<br/>" else ""

		waterStr = StringBuilder()

		if (additives.size > 0)
		{
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_additives))
			waterStr.append("</b>")

			additives.forEach { additive ->
				if (additive.amount == null) return@forEach

				val converted = Unit.ML.to(measureUnit, additive.amount!!)
				val amountStr = if (converted == Math.floor(converted)) converted.toInt().toString() else converted.toString()

				waterStr.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;• ")
				waterStr.append(additive.description)
				waterStr.append("  -  ")
				waterStr.append(amountStr)
				waterStr.append(measureUnit.label)
				waterStr.append("/")
				waterStr.append(deliveryUnit.label)
			}
		}

		summary += waterStr.toString()

		return summary
	}
}
