package me.anon.lib.ext

import kotlin.math.round

public fun Double.round(decimals: Int): Double
{
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return round(this * multiplier) / multiplier
}

public fun Double.formatWhole(): String
{
	if (this.toDouble() - this.toInt().toDouble() == 0.0) return "${this.toInt()}"
	return "${this}"
}
