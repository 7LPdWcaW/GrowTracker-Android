package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.combine
import javax.inject.Inject

class CropViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<CropViewModel>
	{
		override fun create(handle: SavedStateHandle): CropViewModel =
			CropViewModel(diariesRepository, handle)
	}

	private var _diaryId: MutableLiveData<String> = savedStateHandle.getLiveData("diary_id")
	private var _cropId: MutableLiveData<String> = savedStateHandle.getLiveData("crop_id")

	public val diary = _diaryId.switchMap { id ->
		liveData {
			val diary = diariesRepository.getDiaryById(id) ?: throw IllegalArgumentException("Diary $id not found")
			emit(diary)
		}
	}
	public val crop = _cropId.combine(diary) { cropId, diary ->
		if (cropId.isBlank())
		{
			val crop = Crop(name = "", genetics = "")
			diary.crops += crop
			_cropId.postValue(crop.id)
			crop
		}
		else
		{
			diary.crop(cropId)
		}
	}

	public fun init(diaryId: String, cropId: String? = null)
	{
		_diaryId.postValue(diaryId)
		_cropId.postValue(cropId ?: "")
	}
}
