package me.anon.grow3.data.repository.impl

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.repository.GardensRepository
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.util.DataResult
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * // TODO: Add class description
 */
@Singleton
class DefaultGardensRepository @Inject constructor(
	private val dataSource: GardensDataSource,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : GardensRepository
{
	override fun loaded(): LiveData<DataResult<Boolean>> = dataSource.loaded()
	override fun observeGardens(): LiveData<DataResult<List<Garden>>> = dataSource.observeGardens()
	override suspend fun getGardens(): List<Garden> = dataSource.getGardens()
}
