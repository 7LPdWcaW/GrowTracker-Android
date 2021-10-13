package me.anon.grow3.ui.action.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.*
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.Extras.EXTRA_LOG_TYPE
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.nameOf
import me.anon.grow3.util.states.DataResult
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

	sealed class UiResult
	{
		data class Loaded(val diary: Diary, val log: Log): UiResult()
		object Loading : UiResult()
	}

	public var isNew: Boolean = false
		get() = savedState["new_log"] ?: false
		private set(value) {
			savedState["new_log"] = value
			field = value
		}

	public var diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: ""; private set
	public var logId: String = savedState[Extras.EXTRA_LOG_ID] ?: ""; private set
	private val logType: String = savedState[EXTRA_LOG_TYPE] ?: throw InvalidLogType()

	private var _state = MutableStateFlow<UiResult>(UiResult.Loading)
	public val state: StateFlow<UiResult> = _state

	init {
		viewModelScope.launch {
			if (logId.isEmpty())
			{
				isNew = true
			}

			val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed()

			if (isNew)
			{
				val newLog: Log = when (logType)
				{
					nameOf<Water>() -> Water { }
					nameOf<StageChange>() -> StageChange(diary.stage().type)
					nameOf<Photo>() -> Photo()
					else -> throw InvalidLogType()
				}

				newLog.isDraft = true
				logId = diariesRepository.addLog(newLog, diary).id
			}

			diariesRepository.flowDiary(diaryId)
				.mapLatest {
					when (it)
					{
						is DataResult.Success -> it.data
						else -> throw DiaryLoadFailed()
					}
				}
				.collectLatest {
					val diary = it
					if (logId.isEmpty()) return@collectLatest

					diariesRepository.getLog(logId, diary)?.let { log ->
						_state.emit(UiResult.Loaded(diary, log))
					}
				}
		}
	}

	public fun remove()
	{
		viewModelScope.launch {
			if (logId.isEmpty()) return@launch
			val diary = (_state.value as? UiResult.Loaded)?.diary ?: return@launch
			diariesRepository.removeLog(logId, diary)
		}
	}

	public fun save(new: Log)
	{
		isNew = false
		viewModelScope.launch {
			val diary = (_state.value as? UiResult.Loaded)?.diary ?: return@launch
			new.isDraft = false
			diariesRepository.addLog(new, diary)
		}
	}

	public fun clear()
	{
		if (isNew || (state.value as? UiResult.Loaded)?.log?.isDraft == true)
		{
			remove()
		}

		logId = ""
		diaryId = ""
		isNew = false
	}
}
