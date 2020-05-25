package me.anon.grow3.data.repository.impl

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.DataResult
import me.anon.grow3.util.asFailure
import me.anon.grow3.util.asSuccess
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DefaultDiariesRepository @Inject constructor(
	private val dataSource: DiariesDataSource,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DiariesRepository
{
	private val _loaded = MutableLiveData(false)
	private val _diaries: LiveData<DataResult<List<Diary>>> = _loaded.switchMap { isLoaded ->
		liveData {
			if (isLoaded == false)
			{
				emit(DataResult.Loading)
				dataSource.sync(DiariesDataSource.SyncDirection.LOAD)
				_loaded.postValue(true)
				return@liveData
			}

			emit(DataResult.success(dataSource.getDiaries()))
		}
	}

	override fun observeDiaries(): LiveData<DataResult<List<Diary>>>
	{
		if (_loaded.value != true) invalidate()
		return _diaries
	}

	override fun observeDiary(diaryId: String): LiveData<DataResult<Diary>> = _diaries.map {
		when (it)
		{
			is DataResult.Success -> DataResult.success(it.data.find { garden -> garden.id == diaryId }!!)
			is DataResult.Loading -> it
			else -> it.asFailure()
		}
	}

	override suspend fun getDiaries(): List<Diary> = dataSource.getDiaries()

	override suspend fun getDiaryById(diaryId: String): Diary? = dataSource.getDiaryById(diaryId)

	override suspend fun createDiary(diary: Diary, isDraft: Boolean): Diary
	{
		if (isDraft) return dataSource.addTempDiary(diary)
		return dataSource.addDiary(diary).find { it.id == diary.id }!!
	}

	override fun sync()
	{
		_diaries.value?.asSuccess()?.let { diaries ->
			CoroutineScope(dispatcher).launch {
				dataSource.sync(DiariesDataSource.SyncDirection.SAVE, *diaries.toTypedArray())
				invalidate()
			}
		}
	}

	override fun invalidate()
	{
		_loaded.postValue(false)
	}
}
