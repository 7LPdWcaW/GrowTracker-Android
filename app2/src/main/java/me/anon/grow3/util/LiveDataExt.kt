package me.anon.grow3.util

import androidx.annotation.MainThread
import androidx.lifecycle.*

public fun <T> MutableLiveData<T>.asLiveData()
	= this as LiveData<T>

public fun <T> LiveData<T>.asMutableLiveData()
	= this as MutableLiveData<T>

public fun <T> MutableLiveData<T>.notifyChange() = this.postValue(this.value)

public fun merge(vararg liveData: LiveData<*>): LiveData<ArrayList<*>>
	= arrayListOf(*liveData).zipMapLiveData()

public fun <J> ArrayList<LiveData<J>>.zipLiveData(): LiveData<ArrayList<J>>
{
	return MediatorLiveData<ArrayList<J>>().apply {
		val zippedObjects = ArrayList<J>()
		this@zipLiveData.forEach {
			addSource(it) { item ->
				synchronized(zippedObjects)
				{
					zippedObjects.add(item)

					if (zippedObjects.size == this@zipLiveData.size)
					{
						value = zippedObjects
					}
				}
			}
		}
	}
}

public fun List<LiveData<*>>.zipMapLiveData(): LiveData<ArrayList<*>>
{
	return MediatorLiveData<ArrayList<*>>().apply {
		val zippedObjects = ArrayList<Any>()
		this@zipMapLiveData.forEach {
			addSource(it) { item ->
				if (!zippedObjects.contains(item))
				{
					zippedObjects.add(item)
				}

				if (zippedObjects.size == this@zipMapLiveData.size)
				{
					value = zippedObjects
				}
			}
		}
	}
}

public fun <T, A, B> LiveData<A>.combine(other: LiveData<B>, onChange: (A, B) -> T): MediatorLiveData<T>
{
	var source1emitted = false
	var source2emitted = false

	val result = MediatorLiveData<T>()

	val mergeF = {
		val source1Value = this.value
		val source2Value = other.value

		if (source1emitted && source2emitted)
		{
			result.value = onChange.invoke(source1Value!!, source2Value!!)
		}
	}

	result.addSource(this) {
		source1emitted = true
		mergeF()
	}

	result.addSource(other) {
		source2emitted = true
		mergeF()
	}

	return result
}

@MainThread
inline fun <T> LiveData<T>.observeOnce(
	owner: LifecycleOwner,
	crossinline onChanged: (T) -> Unit
): Observer<T>
{
	val wrappedObserver = object : Observer<T> {
		override fun onChanged(t: T)
		{
			onChanged.invoke(t)
			this@observeOnce.removeObserver(this)
		}
	}

	observe(owner, wrappedObserver)
	return wrappedObserver
}

public fun <T> LiveData<T>.clear(): T?
{
	val value = (this as? MutableLiveData)?.value
	(this as? MutableLiveData)?.postValue(null)
	return value
}
