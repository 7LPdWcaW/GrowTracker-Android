package me.anon.grow3.data.repository.impl

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.repository.GardensRepository
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.util.DataResult
import me.anon.grow3.util.asFailure
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
	private val _loaded = MutableLiveData<Boolean>(false)
	private val _gardens: LiveData<DataResult<List<Garden>>> = _loaded.switchMap { isLoaded ->
		liveData {
			if (isLoaded == false)
			{
				emit(DataResult.Loading)
				dataSource.sync(GardensDataSource.SyncDirection.LOAD)
				_loaded.postValue(true)
				return@liveData
			}

			emit(DataResult.success(dataSource.getGardens()))
		}
	}

	public fun observeGardens(): LiveData<DataResult<List<Garden>>>
	{
		if (_loaded.value != true)
		{
			invalidate()
		}

		return _gardens
	}

	public fun observeGarden(gardenId: String): LiveData<DataResult<Garden>> = _gardens.map {
		when (it)
		{
			is DataResult.Success -> DataResult.success(it.data.find { garden -> garden.id == gardenId }!!)
			is DataResult.Loading -> it
			else -> it.asFailure()
		}
	}

	public suspend fun getGardens(): List<Garden> = dataSource.getGardens()

	public suspend fun getGardenById(gardenId: String): Garden? = dataSource.getGardenById(gardenId)

	public suspend fun createGarden(garden: Garden): Garden = dataSource.addGarden(garden).find { it.id == garden.id }!!

	public fun syncToDisk()
	{
		GlobalScope.launch {
			dataSource.sync(GardensDataSource.SyncDirection.SAVE)
			invalidate()
		}
	}

	public fun invalidate()
	{
		_loaded.postValue(false)
	}
}
