package me.anon.grow3.ui.crops.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras.EXTRA_CROP_ID
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asFailure
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.ofSuccess
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

	private val diaryId: String = savedState[EXTRA_DIARY_ID] ?: throw kotlin.IllegalArgumentException("No diary id set")
	private val cropId: String = savedState[EXTRA_CROP_ID] ?: throw kotlin.IllegalArgumentException("No crop id set")
	public val diary = diariesRepository.observeDiary(diaryId)
	public val crop: LiveData<DataResult<Crop>> = diary.map {
		when(it)
		{
			is DataResult.Success -> it.asSuccess().crop(cropId).ofSuccess()
			else -> it.asFailure()
		}
	}
}
