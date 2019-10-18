package me.anon.lib

import android.content.Context
import android.preference.PreferenceManager
import me.anon.grow.R
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Unit class used for conditivity measurement
 */
enum class TdsUnit private constructor(val strRes: Int, val enStr: String, val label: String, val decimalPlaces: Int = 0)
{
	PPM500(R.string.ppm500_description, "PPM500", "ppm", 0),
	PPM700(R.string.ppm700_description, "PPM700", "ppm", 0),
	EC(R.string.ec_description, "EC", "mS/cm", 2);

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
