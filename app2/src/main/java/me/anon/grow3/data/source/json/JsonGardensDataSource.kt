package me.anon.grow3.data.source.json

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.util.loadAsGardens
import me.anon.grow3.util.saveAsGardens
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class JsonGardensDataSource @Inject constructor(
	@Named("garden_source") private val sourcePath: String,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : GardensDataSource
{
	private var lastSynced = -1L
	private var _gardens: MutableList<Garden>? = null

	private val gardens: MutableLiveData<List<Garden>> = MutableLiveData(_gardens ?: arrayListOf())

	override suspend fun addGarden(garden: Garden): List<Garden>
	{
		with (getGardens() as MutableList) {
			if (contains(garden)) throw IllegalArgumentException("Garden ${garden.id} already exists")

			add(garden)
		}

		return sync(GardensDataSource.SyncDirection.SAVE)
	}

	override suspend fun getGardenById(gardenId: String): Garden? = getGardens().find { it.id == gardenId }

	override suspend fun sync(direction: GardensDataSource.SyncDirection): List<Garden>
	{
		withContext(dispatcher) {
			when (direction)
			{
				GardensDataSource.SyncDirection.SAVE -> {
					(_gardens ?: arrayListOf()).saveAsGardens(File(sourcePath))
					lastSynced = System.currentTimeMillis()
				}

				GardensDataSource.SyncDirection.LOAD -> {
					_gardens = null
					lastSynced = -1
				}
			}
		}

		return getGardens()
	}

	override suspend fun getGardens(): List<Garden>
	{
		if (_gardens == null || lastSynced == -1L)
		{
			_gardens = loadFromDisk() as MutableList<Garden>
			lastSynced = System.currentTimeMillis()
			gardens.postValue(_gardens)
		}

		return _gardens!!
	}

	private suspend fun loadFromDisk(): List<Garden> = withContext(dispatcher) {
		File(sourcePath).loadAsGardens { arrayListOf() }
	}
}
