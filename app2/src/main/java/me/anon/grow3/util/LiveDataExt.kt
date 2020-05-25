package me.anon.grow3.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

public fun <T> MutableLiveData<T>.toLiveData()
	= this as LiveData<T>

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
