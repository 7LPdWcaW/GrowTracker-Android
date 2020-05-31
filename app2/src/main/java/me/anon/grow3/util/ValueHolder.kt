package me.anon.grow3.util

class ValueHolder<T> (
	var value: T,
	var setValue: Boolean = true
)
{
	fun applyValue(block: (T) -> Unit) { if (setValue) block(value) }
}
