package me.anon.grow3.util

public inline fun <reified T : Any> codeOf(): Int = T::class.java.name.hashCode().and(0xffff)
public inline fun <reified T : Any> nameOf(): String = T::class.java.name

public fun <I, O> I.transform(block: I.() -> O): O = block(this)

public fun Any?.toStringOrNull(): String? = this?.toString()?.takeIf { it.isNotBlank() }
