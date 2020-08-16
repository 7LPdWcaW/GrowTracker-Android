package me.anon.grow3.ui.action.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.*
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.Water
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.source.CacheDataSource
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.common.Extras.EXTRA_LOG_ID
import me.anon.grow3.ui.common.Extras.EXTRA_LOG_TYPE
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.nameOf
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
import javax.inject.Inject

class LogActionViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val cacheData: CacheDataSource,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository,
		private val cacheData: CacheDataSource
	) : ViewModelFactory<LogActionViewModel>
	{
		override fun create(handle: SavedStateHandle): LogActionViewModel =
			LogActionViewModel(diariesRepository, cacheData, handle)
	}

	private val diaryId: String = savedState[EXTRA_DIARY_ID] ?: throw InvalidDiaryId()
	private var logId: String? = savedState[EXTRA_LOG_ID]
		set(value) {
			field = value
			savedState[EXTRA_LOG_ID] = value
		}

	private val logType: String = savedState[EXTRA_LOG_TYPE] ?: throw InvalidLogType()

	public val diary = liveData<Diary> {
		emitSource(diariesRepository.observeDiary(diaryId).map { result ->
			when (result)
			{
				is DataResult.Success -> result.asSuccess()
				else -> throw DiaryLoadFailed(diaryId)
			}
		})
	}

	public val log: LiveData<Log> = diary.switchMap { diary ->
		liveData<Log> {
			val log = if (logId == null)
			{
				val newLog: Log = when (logType)
				{
					nameOf<Water>() -> Water { }
					nameOf<StageChange>() -> StageChange(diary.stage().type)
					else -> return@liveData
				}

				logId = newLog.id
				diariesRepository.addLog(newLog, diary)
			}
			else
			{
				diariesRepository.getLog(logId!!, diary) ?: throw LogLoadFailed(logId!!)
			}

			emit(log)
		}
	}

	public fun saveLog(draft: Boolean = false)
	{
		log.value ?: return

		viewModelScope.launch {
			if (draft)
			{
				cacheData.cache(log.value!!)
			}
			else
			{
				diary.value?.let {
					diariesRepository.addLog(log.value!!, it)
				}
			}
		}
	}
}
