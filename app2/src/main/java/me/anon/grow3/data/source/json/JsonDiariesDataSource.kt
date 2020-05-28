package me.anon.grow3.data.source.json

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.loadAsDiaries
import me.anon.grow3.util.saveAsDiaries
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class JsonDiariesDataSource @Inject constructor(
	@Named("garden_source") private val sourcePath: String,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DiariesDataSource
{
	private var lastSynced = -1L
	private var _diaries: MutableList<Diary>? = null

	private val diaries: MutableLiveData<List<Diary>> = MutableLiveData(_diaries ?: arrayListOf())

	override fun close()
	{
		runBlocking { sync(DiariesDataSource.SyncDirection.SAVE) }
		_diaries = null
		diaries.postValue(arrayListOf())
	}

	override suspend fun addDiary(diary: Diary): List<Diary>
	{
		with (getDiaries() as MutableList) {
			if (contains(diary)) throw IllegalArgumentException("Diary ${diary.id} already exists")

			add(diary)
		}

		return sync(DiariesDataSource.SyncDirection.SAVE)
	}

	override suspend fun getDiaryById(diaryId: String): Diary? = getDiaries().find { it.id == diaryId }

	suspend fun sync(direction: DiariesDataSource.SyncDirection): List<Diary>
		= sync(direction, *(_diaries ?: arrayListOf()).map { it }.toTypedArray())

	override suspend fun sync(direction: DiariesDataSource.SyncDirection, vararg diary: Diary): List<Diary>
	{
		withContext(dispatcher) {
			when (direction)
			{
				DiariesDataSource.SyncDirection.SAVE -> {
					(_diaries ?: arrayListOf()).saveAsDiaries(File(sourcePath))
					lastSynced = System.currentTimeMillis()
				}

				DiariesDataSource.SyncDirection.LOAD -> {
					_diaries = null
					lastSynced = -1
				}
			}
		}

		return getDiaries()
	}

	override suspend fun getDiaries(): List<Diary>
	{
		if (_diaries == null || lastSynced == -1L)
		{
			_diaries = loadFromDisk() as MutableList<Diary>
			lastSynced = System.currentTimeMillis()
			diaries.postValue(_diaries)
		}

		return _diaries!!
	}

	private suspend fun loadFromDisk(): List<Diary> = withContext(dispatcher) {
		File(sourcePath).loadAsDiaries { arrayListOf() }
	}
}
