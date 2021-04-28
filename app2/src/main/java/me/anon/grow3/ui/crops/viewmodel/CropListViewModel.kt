package me.anon.grow3.ui.crops.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
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
	}

	public val diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: throw InvalidDiaryId()
	public val state: LiveData<UiResult> = diariesRepository.flowDiary(diaryId)
		.asLiveData(viewModelScope.coroutineContext)
		.switchMap { result ->
			liveData<UiResult> {
				when (result)
				{
					is DataResult.Success -> emit(UiResult.Loaded(result.data, result.data.crops))
					else -> throw DiaryLoadFailed(diaryId)
				}
			}
		}
}
