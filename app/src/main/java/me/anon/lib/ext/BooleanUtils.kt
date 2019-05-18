package me.anon.lib.ext

import android.view.View

public fun Boolean?.asVisibility(): Int
{
	return when (this)
	{
		true -> View.VISIBLE
		else -> View.GONE
	}
}

public fun <R> Boolean?.toValue(trueValue: Any?, falseValue: Any?): R
{
	return when (this)
	{
		true -> trueValue as R
		false, null -> falseValue as R
	}
}

/**
 * Ternary implementation
 * Usage: <bool val> t <true val> ?: <false val>
 */
@Suppress("FunctionName")
public infix fun <T : Any> Boolean?.T(value: T): T? = if (this == true) value else null
