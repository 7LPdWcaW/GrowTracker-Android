package me.anon.grow3.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.util.DataResult

/**
 * // TODO: Add class description
 */
class FakeGardenDataSource(private val gardens: List<Garden>) : GardensDataSource
{
	override fun loaded(): LiveData<DataResult<Boolean>> = MutableLiveData(DataResult.Success(false))
	override fun observeGardens(): LiveData<DataResult<List<Garden>>> = MutableLiveData(DataResult.Success(gardens))

	override suspend fun getGardens(): List<Garden> = gardens
}
