package me.anon.lib

import android.content.Context
import android.preference.PreferenceManager
import me.anon.grow.R
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Unit class used for conditivity measurement
 */
enum class TdsUnit private constructor(val strRes: Int, val label: String)
{
	PPM500(R.string.ppm500_description, "ppm"),
	PPM700(R.string.ppm700_description, "ppm"),
	EC(R.string.ec_description, "mS/cm");

	companion object
	{
		@JvmStatic
		fun toTwoDecimalPlaces(input: Double): Double
		{
			return if (java.lang.Double.isInfinite(input) || java.lang.Double.isNaN(input)) 0.0 else BigDecimal(input).setScale(2, RoundingMode.HALF_EVEN).toDouble()
		}

		@JvmStatic
		fun getSelectedTdsUnit(context: Context): TdsUnit
		{
			val index: Int = PreferenceManager.getDefaultSharedPreferences(context).getInt("tds_unit", -1)
			return values()[if (index == -1) PPM500.ordinal else index]
		}
	}
}
