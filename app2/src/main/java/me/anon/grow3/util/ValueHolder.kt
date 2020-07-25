package me.anon.grow3.util

/**
 * Value holder class for models where [null] is an acceptable type,
 * but need to not be set when passing a [null] through method parameters.
 *
 * Example:
 *
 * ```
 * 	fun save(varA: ValueHolder<Int>? = null, varB: ValueHolder<Int>? = null)
 * 	{
 * 		varA?.applyValue { saveVal(it.value) }
 * 		varB?.applyValue { saveVal(it.value) }
 * 	}
 *
 * 	save(varB = ValueHolder(1))
 * 	save(varA = 123.hold())
 * ```
 */
class ValueHolder<T> (
	var value: T,
	var setValue: Boolean = true
)
{
	fun applyValue(block: (T) -> Unit) { if (setValue) block(value) }
}

public fun <T> T.hold(): ValueHolder<T> = ValueHolder(this)
