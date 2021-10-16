package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.diaries.adapter.DiaryListAdapter
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class DiaryListViewModel constructor(
	private val diariesRepository: DiariesRepository,
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository,
	) : ViewModelFactory<DiaryListViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryListViewModel =
			DiaryListViewModel(diariesRepository)
	}

	sealed class UiState
	{
		object Loading : UiState()
		data class Loaded(val diaries: List<DiaryListAdapter.DiaryStub>) : UiState()
	}

	private val _state = MutableStateFlow<UiState>(UiState.Loading)
	public val state: Flow<UiState> = _state

	init {
		viewModelScope.launch {
			diariesRepository.flowDiaries()
				.collectLatest { result ->
					when (result)
					{
						is DataResult.Success -> {
							val stubs = result.data.map {
								DiaryListAdapter.DiaryStub(it.id, it.name, it.shortMenuSummary())
							}
							_state.emit(UiState.Loaded(stubs))
						}
						else -> throw GrowTrackerException.DiaryLoadFailed()
					}
				}
		}
	}
}
