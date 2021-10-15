package me.anon.lib.ext

import android.content.Context
import android.util.TypedValue
import kotlin.math.round

public fun Double.round(decimals: Int): Double
{
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return round(this * multiplier) / multiplier
}

public fun Number?.formatWhole(): String
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
	} ?: "0"
}

public fun Long.toDays(): Double = (this / (1000.0 * 60.0 * 60.0 * 24.0))

public fun Number.dip(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)
public fun Number.sp(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics)
public fun Number.mm(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, this.toFloat(), context.resources.displayMetrics)
public fun Number.pt(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, this.toFloat(), context.resources.displayMetrics)

public fun Double?.max(other: Double?): Double?
{
	return when
	{
		other == null -> this
		this == null -> other
		else -> kotlin.math.max(this, other)
	}
}

public fun Double?.min(other: Double?): Double?
{
	return when
	{
		other == null -> this
		this == null -> other
		else -> kotlin.math.min(this, other)
	}
}
