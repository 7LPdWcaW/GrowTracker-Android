package me.anon.grow3.ui.main.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class MainViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<MainViewModel>
	{
		override fun create(handle: SavedStateHandle): MainViewModel =
			MainViewModel(diariesRepository, handle)
	}

	sealed class UiState
	{
		object Loading : UiState()
		object EmptyDiaries : UiState()
		data class ViewDiary(val diaryId: String): UiState()
	}

	public val logEvents = diariesRepository.flowLogEvents()

	private val _state = MutableStateFlow<UiState>(UiState.Loading)
	public val state: StateFlow<UiState> = _state

	init {
		viewModelScope.launch {
			val diaries = diariesRepository.flowDiaries().firstOrNull()
			if (diaries is DataResult.Success && diaries.data.lastOrNull() != null)
			{
				// most recent
				_state.emit(UiState.ViewDiary(diaries.data.last().id))
			}
			else
			{
				_state.emit(UiState.EmptyDiaries)
			}
		}
	}
}
