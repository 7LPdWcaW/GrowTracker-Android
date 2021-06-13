package me.anon.grow3.ui.crops.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.exceptions.GrowTrackerException.InvalidDiaryId
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class CropListViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<CropListViewModel>
	{
		override fun create(handle: SavedStateHandle): CropListViewModel =
			CropListViewModel(diariesRepository, handle)
	}

	sealed class UiResult
	{
		data class Loaded(val diary: Diary, val crops: List<Crop>) : UiResult()
		object Loading : UiResult()
	}

	public val diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: throw InvalidDiaryId()

	private var _state = MutableStateFlow<UiResult>(UiResult.Loading)
	public val state: StateFlow<UiResult> = _state

	init {
		viewModelScope.launch {
			diariesRepository.flowDiary(diaryId)
				.mapLatest {
					when (it)
					{
						is DataResult.Success -> it.data
						else -> throw GrowTrackerException.DiaryLoadFailed()
					}
				}
				.collectLatest {
					_state.emit(UiResult.Loaded(it, it.crops))
				}
		}
	}
}
