package me.anon.grow3.ui.crops.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.exceptions.GrowTrackerException.*
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras.EXTRA_CROP_ID
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class ViewCropViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<ViewCropViewModel>
	{
		override fun create(handle: SavedStateHandle): ViewCropViewModel =
			ViewCropViewModel(diariesRepository, handle)
	}

	sealed class UiResult
	{
		data class Loaded(val diary: Diary, val crop: Crop) : UiResult()
	}

	private val diaryId: String = savedState[EXTRA_DIARY_ID] ?: throw InvalidDiaryId()
	private val cropId: String = savedState[EXTRA_CROP_ID] ?: throw InvalidCropId()

	public val state = diariesRepository.flowDiary(diaryId)
		.asLiveData(viewModelScope.coroutineContext)
		.switchMap { result ->
			liveData<UiResult> {
				when (result)
				{
					is DataResult.Success -> emit(UiResult.Loaded(result.data, result.data.crop(cropId)))
					else -> throw DiaryLoadFailed(diaryId)
				}
			}
		}
}
