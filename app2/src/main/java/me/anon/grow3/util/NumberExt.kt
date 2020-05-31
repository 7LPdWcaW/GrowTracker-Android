package me.anon.grow3.util

public fun Double.round(decimals: Int): Double
{
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return kotlin.math.round(this * multiplier) / multiplier
}

public fun Number?.asString(): String = this.asStringOrNull() ?: "0"

public fun Number?.asStringOrNull(): String?
{
	return this?.let {
		if (this.toDouble() - this.toInt().toDouble() == 0.0) return "${this.toInt()}"
		return when (this)
		{
			is Double -> {
				val value = this.round(2)
				return if (value - value.toInt().toDouble() == 0.0) "${value.toInt()}" else "${value}"
			}
			is Float -> {
				val value = this.toDouble().round(2)
				return if (value - value.toInt().toDouble() == 0.0) "${value.toInt()}" else "${value}"
			}
			else -> "${this.toInt()}"
		}
	}
}
