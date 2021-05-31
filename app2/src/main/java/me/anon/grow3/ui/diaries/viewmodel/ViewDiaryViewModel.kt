package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class ViewDiaryViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<ViewDiaryViewModel>
	{
		override fun create(handle: SavedStateHandle): ViewDiaryViewModel =
			ViewDiaryViewModel(diariesRepository, handle)
	}

	sealed class UiResult
	{
		data class Loaded(val diary: Diary): UiResult()
		object Loading : UiResult()
		object Removed : UiResult()
	}

	public var diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: ""; private set
	private var diary: Flow<Diary> = diariesRepository.flowDiary(diaryId)
		.mapLatest {
			when (it)
			{
				is DataResult.Success -> it.data
				else -> throw GrowTrackerException.DiaryLoadFailed()
			}
		}

	private var _state = MutableStateFlow<UiResult>(UiResult.Loading)
	public val state: StateFlow<UiResult> = _state

	init {
		viewModelScope.launch {
			diary
				.catch { cause ->
					if (cause is GrowTrackerException.DiaryLoadFailed) _state.emit(UiResult.Removed)
				}
				.collectLatest {
					_state.emit(UiResult.Loaded(it))
				}
		}
	}
}
