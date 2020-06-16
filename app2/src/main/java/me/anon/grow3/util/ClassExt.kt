package me.anon.grow3.util

public inline fun <reified T : Any> code(): Int = T::class.java.name.hashCode().and(0xffff)
public inline fun <reified T : Any> name(): String = T::class.java.name

public fun <I, O> I.transform(block: I.() -> O): O = block(this)

public fun Any?.toStringOrNull(): String? = this?.toString()?.takeIf { it.isNotBlank() }
