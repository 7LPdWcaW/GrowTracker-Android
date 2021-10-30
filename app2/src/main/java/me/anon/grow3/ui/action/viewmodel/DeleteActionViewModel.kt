package me.anon.grow3.ui.action.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.*
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ViewModelFactory
import javax.inject.Inject

class DeleteActionViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<DeleteActionViewModel>
	{
		override fun create(handle: SavedStateHandle): DeleteActionViewModel =
			DeleteActionViewModel(diariesRepository, handle)
	}

	sealed class UiResult
	{
		object Confirm : UiResult()
		object Deleted : UiResult()
	}

	public var diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: ""; private set
	public var logId: String = savedState[Extras.EXTRA_LOG_ID] ?: ""; private set

	private var _state = MutableStateFlow<UiResult>(UiResult.Confirm)
	public val state: StateFlow<UiResult> = _state

	public fun remove()
	{
		viewModelScope.launch {
			if (logId.isEmpty()) return@launch

			val diary = diariesRepository.getDiaryById(diaryId) ?: return@launch
			diariesRepository.removeLog(logId, diary)
			_state.emit(UiResult.Deleted)
		}
	}
}
