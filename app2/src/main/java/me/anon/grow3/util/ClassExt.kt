package me.anon.grow3.util

import me.anon.grow3.di.ApplicationComponent

typealias Injector = (ApplicationComponent) -> Unit

public fun codeOf(any: Any): Int = any.toString().toCharArray().sumBy { it.toInt() }
public inline fun <reified T : Any> codeOf(): Int = T::class.java.name.hashCode().and(0xffff)
public inline fun <reified T : Any> nameOf(): String = T::class.java.name

public inline fun <I, O> I.transform(crossinline block: I.() -> O): O = block(this)

public fun Any?.toStringOrNull(): String? = this?.toString()?.takeIf { it.isNotBlank() }

public suspend fun <T> tryNull(block: suspend () -> T?): T?
{
	try
	{
		return block()
	}
	catch (e: Exception)
	{
		return null
	}
}

