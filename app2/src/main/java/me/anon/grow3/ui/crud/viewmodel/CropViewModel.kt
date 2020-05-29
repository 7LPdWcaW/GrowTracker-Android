package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.*
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.isSuccess
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

	private var newCrop = savedStateHandle.get("new_crop") ?: false
		set(value) { field = value.apply { savedStateHandle["new_crop"] = this } }
	private var cropComparator = savedStateHandle.get("crop_str") ?: ""
		set(value) { field = value.apply { savedStateHandle["crop_str"] = this } }

	private var _diaryId: MutableLiveData<String> = savedStateHandle.getLiveData("diary_id")
	private var _cropId: MutableLiveData<String> = savedStateHandle.getLiveData("crop_id")

	public val diary = _diaryId.switchMap { id ->
		diariesRepository.observeDiary(id)
	}

	public val crop = _cropId.combine(diary) { cropId, diary ->
		if (!diary.isSuccess) throw IllegalArgumentException("Unable to load diary")
		val diary = diary.asSuccess()
		if (cropId.isBlank())
		{
			newCrop = true
			val crop = Crop(name = "", genetics = "")
			diary.crops += crop
			_cropId.postValue(crop.id)
			cropComparator = crop.toJsonString()
			diariesRepository.sync()
			crop
		}
		else
		{
			diary.crop(cropId).also {
				cropComparator = it.toJsonString()
			}
		}
	}

	public fun init(diaryId: String, cropId: String? = null)
	{
		_diaryId.postValue(diaryId)
		_cropId.postValue(cropId ?: "")
	}

	/**
	 * Removes the temp crop from the diary and re-syncs
	 */
	public fun reset()
	{
		if (!newCrop) return

		// remove if nothing has been added
		if (cropComparator == crop.value?.toJsonString())
		{
			diary.value?.asSuccess()?.let { diary ->
				diary.crops.removeAll { it.id == _cropId.value }
			}
		}
	}

	public fun save()
	{
		diariesRepository.sync()
	}
}
