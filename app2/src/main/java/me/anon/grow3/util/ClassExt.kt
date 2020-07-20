package me.anon.grow3.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import me.anon.grow3.di.ApplicationComponent
import kotlin.reflect.KClass

typealias Injector = (ApplicationComponent) -> Unit

public fun codeOf(any: Any): Int = any.toString().toCharArray().sumBy { it.toInt() }
public inline fun <reified T : Any> codeOf(): Int = T::class.java.name.hashCode().and(0xffff)
public inline fun <reified T : Any> nameOf(): String = T::class.java.name

public inline fun <I, O> I.transform(crossinline block: I.() -> O): O = block(this)

public fun Any?.toStringOrNull(): String? = this?.toString()?.takeIf { it.isNotBlank() }

public inline fun <reified T: ViewBinding> KClass<ViewBinding>.inflate(container: ViewGroup): T
{
	val viewBinder = this.java.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
		.invoke(this, LayoutInflater.from(container.context), container, false) as ViewBinding
	return viewBinder as T
}

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

