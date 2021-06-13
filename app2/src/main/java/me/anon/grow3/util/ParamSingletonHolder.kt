package me.anon.grow3.util

/**
 * Kotlin singleton holder for class requiring initial parameter
 */
open class ParamSingletonHolder<out T, in A>(creator: (A) -> T)
{
	private var creator: ((A) -> T)? = creator

	@Volatile
	private var instance: T? = null

	public open fun getInstance(): T = instance ?: throw RuntimeException("instance was null. call getInstance(arg) first")

	public open fun getInstance(arg: A): T
	{
		val instance = this.instance
		if (instance != null)
		{
			return instance
		}

		return synchronized(this)
		{
			val instance = this.instance
			if (instance != null)
			{
				instance
			}
			else
			{
				val created = creator!!(arg)
				this.instance = created
				creator = null
				created
			}
		}
	}
}

/**
 * Kotlin singleton holder and creator
 */
open class SingletonHolder<out T>(creator: () -> T)
{
	private var creator: (() -> T)? = creator

	@Volatile
	private var instance: T? = null

	public open fun getInstance(): T
	{
		val instance = this.instance
		if (instance != null)
		{
			return instance
		}

		return synchronized(this)
		{
			val instance = this.instance
			if (instance != null)
			{
				instance
			}
			else
			{
				val created = creator!!()
				this.instance = created
				creator = null
				created
			}
		}
	}
}