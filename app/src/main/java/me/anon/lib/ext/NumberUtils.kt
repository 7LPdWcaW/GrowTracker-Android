package me.anon.lib.ext

import kotlin.math.round

public fun Double.round(decimals: Int): Double
{
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return round(this * multiplier) / multiplier
}

public fun Number.formatWhole(): String
{
	if (this.toDouble() - this.toInt().toDouble() == 0.0) return "${this.toInt()}"
	return when (this)
	{
		is Double -> "${this.round(2)}"
		is Float -> "${this.toDouble().round(2)}"
		else -> "${this.toInt()}"
	}
}
