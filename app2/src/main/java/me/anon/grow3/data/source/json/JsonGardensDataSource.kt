package me.anon.grow3.data.source.json

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.anon.grow3.data.model.Diary
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
	private var _diaries: MutableList<Diary>? = null

	private val gardens: MutableLiveData<List<Diary>> = MutableLiveData(_diaries ?: arrayListOf())

	override suspend fun addGarden(diary: Diary): List<Diary>
	{
		with (getGardens() as MutableList) {
			if (contains(diary)) throw IllegalArgumentException("Garden ${diary.id} already exists")

			add(diary)
		}

		return sync(GardensDataSource.SyncDirection.SAVE)
	}

	override suspend fun getGardenById(gardenId: String): Diary? = getGardens().find { it.id == gardenId }

	override suspend fun sync(direction: GardensDataSource.SyncDirection): List<Diary>
	{
		withContext(dispatcher) {
			when (direction)
			{
				GardensDataSource.SyncDirection.SAVE -> {
					(_diaries ?: arrayListOf()).saveAsGardens(File(sourcePath))
					lastSynced = System.currentTimeMillis()
				}

				GardensDataSource.SyncDirection.LOAD -> {
					_diaries = null
					lastSynced = -1
				}
			}
		}

		return getGardens()
	}

	override suspend fun getGardens(): List<Diary>
	{
		if (_diaries == null || lastSynced == -1L)
		{
			_diaries = loadFromDisk() as MutableList<Diary>
			lastSynced = System.currentTimeMillis()
			gardens.postValue(_diaries)
		}

		return _diaries!!
	}

	private suspend fun loadFromDisk(): List<Diary> = withContext(dispatcher) {
		File(sourcePath).loadAsGardens { arrayListOf() }
	}
}
