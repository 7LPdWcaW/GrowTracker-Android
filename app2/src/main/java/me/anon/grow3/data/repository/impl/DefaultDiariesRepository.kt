package me.anon.grow3.data.repository.impl

import android.content.res.Resources
import androidx.lifecycle.*
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.anon.grow3.data.event.LogEvent
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.source.CacheDataSource
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.tryNull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DefaultDiariesRepository @Inject constructor(
	private val dataSource: DiariesDataSource,
	private val cacheSource: CacheDataSource,
	@Named("io_dispatcher") private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DiariesRepository
{
	private val _refresh = MutableLiveData(true)
	private val _diaries = _refresh.switchMap { force ->
		liveData<DataResult<List<Diary>>> {
			emit(DataResult.Success(dataSource.sync(DiariesDataSource.SyncDirection.LOAD)))
		}
	}
	override fun observeDiaries(): LiveData<DataResult<List<Diary>>> = _diaries

	private val _logEvents: EventEmitter<LogEvent> = EventEmitter()
	override fun observeLogEvents(): EventSource<LogEvent> = _logEvents

	override fun observeDiary(diaryId: String): LiveData<DataResult<Diary>> = _diaries.map {
		if (it is DataResult.Success)
		{
			it.data.firstOrNull { it.id == diaryId }
				?.let { DataResult.success(it) }
				?: DataResult.Error(Resources.NotFoundException())
		}
		else DataResult.Error(Resources.NotFoundException())
	}

	override suspend fun getDiaries(): List<Diary> = dataSource.getDiaries()

	override suspend fun getDiaryById(diaryId: String): Diary? = dataSource.getDiaryById(diaryId)

	override suspend fun addDiary(diary: Diary): Diary
		= dataSource.addDiary(diary)
			.find { it.id == diary.id }!!
			.also {
				invalidate()
			}

	override suspend fun deleteDiary(diaryId: String): Boolean
		= !dataSource.deleteDiary(diaryId)
			.any { it.id == diaryId }
			.also {
				invalidate()
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

	override suspend fun addLog(log: Log, diary: Diary?): Log
	{
		if (diary == null)
		{
			cacheSource.cache(log)
		}
		else
		{
			_logEvents.emit(LogEvent.Added(log, diary))
			diary.log(log)
			dataSource.sync(DiariesDataSource.SyncDirection.SAVE, diary)
		}

		return log
	}

	override suspend fun getLog(logId: String, diary: Diary): Log?
	{
		var cached: Log? = tryNull { cacheSource.retrieveLog(logId) }
		if (cached == null) cached = diary.logOf(logId)
		return cached
	}

	override suspend fun addCrop(crop: Crop, diary: Diary?): Crop
	{
		if (diary == null)
		{
			cacheSource.cache(crop)
		}
		else
		{
			diary.crops as ArrayList += crop
			dataSource.sync(DiariesDataSource.SyncDirection.SAVE, diary)
		}

		return crop
	}

	override suspend fun getCrop(cropId: String, diary: Diary): Crop?
	{
		var cached: Crop? = tryNull { cacheSource.retrieveCrop(cropId) }
		if (cached == null) cached = diary.crop(cropId)
		return cached
	}

	override fun invalidate()
	{
		_refresh.postValue(true)
	}
}
