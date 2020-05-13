package me.anon.grow3.util

public inline fun <reified J> List<Any?>.lastInstanceOf(): J? = this.lastOrNull { it is J } as? J
public inline fun <reified J> List<Any?>.lastInstanceOf(additionalPredicate: (item: J) -> Boolean): J? = this.lastOrNull { it is J && additionalPredicate(it) } as? J

public inline fun <T, R> List<T>.uniqueBy(crossinline predicate: (T) -> R): List<T>
{
	val list = mutableListOf<R>()
	val subList = mutableListOf<T>()
	this.forEach {
		val selector = predicate(it)
		if (!list.contains(selector))
		{
			subList.add(it)
			list.add(selector)
		}
	}

	return subList
}
