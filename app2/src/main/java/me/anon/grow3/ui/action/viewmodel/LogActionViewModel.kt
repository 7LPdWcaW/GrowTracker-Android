package me.anon.grow3.ui.action.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.common.Extras.EXTRA_LOG_ID
import me.anon.grow3.ui.common.Extras.EXTRA_LOG_TYPE
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asFailure
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.ofSuccess
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
	private val logId: String? = savedState[EXTRA_LOG_ID]
	public val logType: String = savedState[EXTRA_LOG_TYPE] ?: throw kotlin.IllegalArgumentException("No log type set")

	public val diary = diariesRepository.observeDiary(diaryId)
	public val log: LiveData<DataResult<Log?>> = diary.map {
		when(it)
		{
			is DataResult.Success -> {
				if (logId == null) DataResult.Success<Log?>(null)
				else it.asSuccess().logOf(logId).ofSuccess()
			}
			else -> it.asFailure()
		}
	}
}
