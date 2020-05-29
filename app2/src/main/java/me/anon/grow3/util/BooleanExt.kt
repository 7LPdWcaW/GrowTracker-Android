package me.anon.grow3.util

public fun <R> Boolean?.toValue(trueValue: Any?, falseValue: Any?): R
{
	return when (this)
	{
		true -> trueValue as R
		false, null -> falseValue as R
	}
}

public inline fun Boolean?.then(block: () -> Unit): Unit = if (this == true) block() else Unit

/**
 * Ternary implementation
 * Usage: <bool val> t <true val> ?: <false val>
 */
@Suppress("FunctionName")
public infix fun <T : Any> Boolean?.T(value: T): T? = if (this == true) value else null
