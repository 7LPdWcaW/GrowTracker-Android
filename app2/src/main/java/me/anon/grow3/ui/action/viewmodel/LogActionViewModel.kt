package me.anon.grow3.ui.action.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.Water
import me.anon.grow3.data.repository.DiariesRepository
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
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<LogActionViewModel>
	{
		override fun create(handle: SavedStateHandle): LogActionViewModel =
			LogActionViewModel(diariesRepository, handle)
	}

	private val diaryId: String = savedState[EXTRA_DIARY_ID] ?: throw kotlin.IllegalArgumentException("No diary id set")
	private var logId: String? = savedState[EXTRA_LOG_ID]
		set(value) {
			field = value
			savedState[EXTRA_LOG_ID] = value
		}

	private val logType: String = savedState[EXTRA_LOG_TYPE] ?: throw kotlin.IllegalArgumentException("No log type set")

	public val diary = diariesRepository.observeDiary(diaryId)
	public val log: LiveData<Log> = diary.switchMap { diaryResult ->
		if (diaryResult is DataResult.Success)
		{
			if (logId == null)
			{
				liveData<Log> {
					var newLog: Log
					when (logType)
					{
						nameOf<Water>() -> {
							newLog = Water { }
						}
						else -> return@liveData
					}

					logId = newLog.id
					diariesRepository.draftLog(newLog)
					emit(newLog)
				}
			}
			else
			{
				MutableLiveData(diaryResult.data.logOf(logId!!) ?: throw IllegalArgumentException("Failed to load log"))
			}
		}
		else
		{
			throw IllegalArgumentException("Failed to load diary")
		}
	}

	public fun saveLog(draft: Boolean = false)
	{
		log.value ?: return

		if (draft)
		{
			viewModelScope.launch {
				diariesRepository.draftLog(log.value!!)
			}
		}
		else
		{
			diary.value?.asSuccess()?.let {
				it.log += log.value!!
				diariesRepository.sync()
			}
		}
	}
}
