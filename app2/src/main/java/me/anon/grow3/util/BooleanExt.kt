package me.anon.grow3.util

public inline fun Boolean?.then(block: () -> Unit): Unit = if (this == true) block() else Unit

/**
 * Ternary implementation
 * Usage: <bool val> then <true val> ?: <false val>
 */
public infix fun <T : Any> Boolean?.then(value: T): T? = if (this == true) value else null
public fun <T : Any> Boolean?.then(trueValue: T?, falseValue: T?): T? = if (this == true) trueValue else falseValue
