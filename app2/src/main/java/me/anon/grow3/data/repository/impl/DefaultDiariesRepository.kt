package me.anon.grow3.data.repository.impl

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.anon.grow3.data.event.LogEvent
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.ParamSingletonHolder
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.tryNull

class DefaultDiariesRepository(
	private val dataSource: DiariesDataSource,
) : DiariesRepository
{
	private val dispatcher: CoroutineDispatcher = Dispatchers.IO
	private val scope = ProcessLifecycleOwner.get().lifecycleScope

	private val _trigger = MutableStateFlow(1)
	private val _logEvents = MutableSharedFlow<LogEvent>(replay = 0)

	override fun flowDiaries(includeDrafts: Boolean): Flow<DataResult<List<Diary>>> = flow {
		val diaries = DataResult.success(getDiaries().filter { diary -> diary.isDraft == includeDrafts || !diary.isDraft })
		emit(diaries)
	}

	override fun flowDiary(diaryId: String): Flow<DataResult<Diary>>
		= _trigger
			.flatMapLatest {
				val diary = getDiaryById(diaryId) ?: throw GrowTrackerException.DiaryLoadFailed(diaryId)
				flowOf(DataResult.success(diary))
			}
			.shareIn(scope, SharingStarted.WhileSubscribed(),1)

	override fun flowLogEvents(): SharedFlow<LogEvent> = _logEvents

	override suspend fun getDiaries(): List<Diary> = dataSource.getDiaries()

	override suspend fun getDiaryById(diaryId: String): Diary? = dataSource.getDiaryById(diaryId)

	override suspend fun addDiary(diary: Diary): Diary
		= dataSource.addDiary(diary)
			.let {
				invalidate()
				diary
			}

	override suspend fun deleteDiary(diaryId: String): Boolean
		= dataSource.deleteDiary(diaryId)
			.let {
				invalidate()
				true
			}

	override suspend fun addLog(log: Log, diary: Diary): Log
	{
		withContext(dispatcher) {
			diary.log(log)

			dataSource.sync(DiariesDataSource.SyncDirection.SAVE, diary)
			invalidate()
		}

		if (!log.isDraft) _logEvents.emit(LogEvent.Added(log, diary))

		return log
	}

	override suspend fun getLog(logId: String, diary: Diary): Log?
	{
		var log: Log?
		withContext(dispatcher) {
			log = diary.logOf(logId)
		}

		return log
	}

	override suspend fun removeLog(logId: String, diary: Diary)
	{
		val index = diary.log.indexOfFirst { it.id == logId }
		if (index > -1)
		{
			(diary.log as ArrayList).removeAt(index)
		}

		dataSource.sync(DiariesDataSource.SyncDirection.SAVE, diary)
		invalidate()
	}

	override suspend fun addCrop(crop: Crop, diary: Diary): Crop
	{
		val index = diary.crops.indexOfFirst { it.id == crop.id }
		if (index > -1)
		{
			(diary.crops as ArrayList)[index] = crop
		}
		else
		{
			diary.crops as ArrayList += crop
		}

		dataSource.sync(DiariesDataSource.SyncDirection.SAVE, diary)
		invalidate()

		return crop
	}

	override suspend fun getCrop(cropId: String, diary: Diary): Crop? = tryNull { diary.crop(cropId) }

	override suspend fun removeCrop(cropId: String, diary: Diary)
	{
		val index = diary.crops.indexOfFirst { it.id == cropId }
		if (index > -1)
		{
			(diary.crops as ArrayList).removeAt(index)
		}

		dataSource.sync(DiariesDataSource.SyncDirection.SAVE, diary)
		invalidate()
	}

	override fun invalidate()
	{
		scope.launch {
			_trigger.emit(_trigger.value + 1)
		}
	}

	companion object : ParamSingletonHolder<DefaultDiariesRepository, DiariesDataSource>(::DefaultDiariesRepository)
	{
		@JvmStatic
		override fun getInstance(): DefaultDiariesRepository = super.getInstance()
	}
}
