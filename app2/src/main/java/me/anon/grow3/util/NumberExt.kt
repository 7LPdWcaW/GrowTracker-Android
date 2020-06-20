package me.anon.grow3.util

import android.content.Context
import android.util.TypedValue

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

public fun Int.dp(context: Context): Int = dip(context).toInt()
public fun Float.dp(context: Context): Float = dip(context)
public fun Double.dp(context: Context): Float = dip(context)
public fun Number.dip(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)

public fun Number.sp(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics)
public fun Number.mm(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, this.toFloat(), context.resources.displayMetrics)
public fun Number.pt(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, this.toFloat(), context.resources.displayMetrics)
