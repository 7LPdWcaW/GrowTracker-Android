package me.anon.grow3.data.repository.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDiariesRepository @Inject constructor(
	private val dataSource: DiariesDataSource,
) : DiariesRepository
{
	private val dispatcher: CoroutineDispatcher = Dispatchers.Main
	private val scope = CoroutineScope(SupervisorJob() + dispatcher)

	private val flows = hashMapOf<String, Flow<DataResult<Diary>>>()
	private val _trigger = MutableStateFlow(1)
	private val _logEvents = MutableSharedFlow<LogEvent>(replay = 0)

	override fun flowDiaries(includeDrafts: Boolean): Flow<DataResult<List<Diary>>> = _trigger.flatMapLatest {
		val diaries = DataResult.success(getDiaries().filter { diary -> diary.isDraft == includeDrafts || !diary.isDraft })
		flowOf(diaries)
	}

	override fun flowDiary(id: String): Flow<DataResult<Diary>>
	{
		var flow = flows[id]
		if (flow == null)
		{
			flow = _trigger
				.mapLatest {
					val diary = getDiaryById(id)
					if (diary != null) DataResult.success(diary)
					else DataResult.error(GrowTrackerException.DiaryLoadFailed(id))
				}

			flows[id] = flow
		}

		return flow.shareIn(scope, SharingStarted.WhileSubscribed(500L), 1)
	}

	override fun flowLogEvents(): SharedFlow<LogEvent> = _logEvents

	override suspend fun getDiaries(): List<Diary>
	{
		return dataSource.getDiaries()
	}

	override suspend fun getDiaryCount(includeDrafts: Boolean): Int
	{
		return getDiaries()
			.filter { diary -> diary.isDraft == includeDrafts || !diary.isDraft }
			.count()
	}

	override suspend fun getDiaryById(diaryId: String): Diary?
	{
		return dataSource.getDiaryById(diaryId)
	}

	override suspend fun addDiary(diary: Diary): Diary
	{
		return dataSource.addDiary(diary)
			.let {
				invalidate()
				diary
			}
	}

	override suspend fun deleteDiary(diaryId: String): Boolean
	{
		return dataSource.deleteDiary(diaryId)
			.let {
				flows.remove(diaryId)
				invalidate()
				true
			}
	}

	override suspend fun addLog(log: Log, diary: Diary): Log
	{
		diary.log(log)

		dataSource.sync(DiariesDataSource.SyncDirection.Commit, diary)
		invalidate()

		if (!log.isDraft) _logEvents.emit(LogEvent.Added(log, diary))
		return log
	}

	override suspend fun getLog(logId: String, diary: Diary): Log?
	{
		return diary.logOf(logId)
	}

	override suspend fun removeLog(logId: String, diary: Diary)
	{
		val index = diary.log.indexOfFirst { it.id == logId }
		if (index > -1)
		{
			(diary.log as ArrayList).removeAt(index)
		}

		dataSource.sync(DiariesDataSource.SyncDirection.Commit, diary)
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

		dataSource.sync(DiariesDataSource.SyncDirection.Commit, diary)
		invalidate()

		return crop
	}

	override suspend fun getCrop(cropId: String, diary: Diary): Crop?
	{
		return tryNull { diary.crop(cropId) }
	}

	override suspend fun removeCrop(cropId: String, diary: Diary)
	{
		val index = diary.crops.indexOfFirst { it.id == cropId }
		if (index > -1)
		{
			(diary.crops as ArrayList).removeAt(index)
		}

		dataSource.sync(DiariesDataSource.SyncDirection.Commit, diary)
		invalidate()
	}

	override suspend fun invalidate()
	{
		_trigger.emit(_trigger.value + 1)
	}

	companion object : ParamSingletonHolder<DefaultDiariesRepository, DiariesDataSource>(::DefaultDiariesRepository)
	{
		@JvmStatic
		override fun getInstance(): DefaultDiariesRepository = super.getInstance()
	}
}