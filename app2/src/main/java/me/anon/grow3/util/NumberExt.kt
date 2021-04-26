package me.anon.grow3.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import me.anon.grow3.BaseApplication
import me.anon.grow3.data.model.Volume

public fun Double.round(decimals: Int): Double
{
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return kotlin.math.round(this * multiplier) / multiplier
}

public fun Volume?.asString(): String = this?.amount.asStringOrNull() ?: "0"
public fun Volume?.asStringOrNull(): String? = this?.amount.asStringOrNull()
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

public val Int.dp: Int get() = dip.toInt()
public val Float.dp: Float get() = dip
public val Double.dp: Double get() = dip.toDouble()
public val Number.dip: Float get() = dip(BaseApplication.context)

public fun Int.dp(context: Context): Int = dip(context).toInt()
public fun Float.dp(context: Context): Float = dip(context)
public fun Double.dp(context: Context): Double = dip(context).toDouble()
public fun Number.dip(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)

public fun Int.dp(view: View): Int = dip(view.context).toInt()
public fun Float.dp(view: View): Float = dip(view.context)
public fun Double.dp(view: View): Double = dip(view.context).toDouble()
public fun Number.dip(view: View): Float = dip(view.context)

public fun Int.dp(fragment: Fragment): Int = dip(fragment.requireContext()).toInt()
public fun Float.dp(fragment: Fragment): Float = dip(fragment.requireContext())
public fun Double.dp(fragment: Fragment): Double = dip(fragment.requireContext()).toDouble()
public fun Number.dip(fragment: Fragment): Float = dip(fragment.requireContext())

public fun Number.sp(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics)
public fun Number.mm(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, this.toFloat(), context.resources.displayMetrics)
public fun Number.pt(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, this.toFloat(), context.resources.displayMetrics)
