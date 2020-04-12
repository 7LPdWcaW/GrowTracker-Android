package me.anon.lib.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * // TODO: Add class description
 */
public fun <J> ArrayList<LiveData<J>>.zipLiveData(): LiveData<ArrayList<J>>
{
	return MediatorLiveData<ArrayList<J>>().apply {
		val zippedObjects = ArrayList<J>()
		this@zipLiveData.forEach {
			addSource(it) { item ->
				if (!zippedObjects.contains(item as J))
				{
					zippedObjects.add(item)
				}

				if (zippedObjects.size == this@zipLiveData.size)
				{
					value = zippedObjects
				}
			}
		}
	}
}

public fun ArrayList<LiveData<Any>>.zipMapLiveData(): LiveData<ArrayList<Any>>
{
	return MediatorLiveData<ArrayList<Any>>().apply {
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
