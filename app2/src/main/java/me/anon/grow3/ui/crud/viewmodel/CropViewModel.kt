package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import com.zhuinden.livedatacombinetuplekt.combineTuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.CropLoadFailed
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.clear
import me.anon.grow3.util.states.Data
import me.anon.grow3.util.states.DataResult

class CropViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle,
	private val viewModelScope: CoroutineScope
)
{
	public var isNew: Boolean = false
		get() = savedStateHandle["new_crop"] ?: false
		private set(value) {
			savedStateHandle["new_crop"] = value
			field = value
		}

	private val diaryId: MutableLiveData<String> = savedStateHandle.getLiveData(Extras.EXTRA_DIARY_ID)
	private val cropId: MutableLiveData<String> = savedStateHandle.getLiveData(Extras.EXTRA_CROP_ID)
	private var originalCrop: Crop? = null
	public val crop: LiveData<Data> = combineTuple(diaryId, cropId).switchMap { (diaryId, cropId) ->
		liveData {
			if (cropId.isNullOrBlank() || diaryId.isNullOrBlank()) return@liveData

			emitSource(diariesRepository.observeDiary(diaryId).switchMap { diaryResult ->
				when (diaryResult)
				{
					is DataResult.Success -> liveData {
						val crop = diaryResult.data.crop(cropId)
						originalCrop = crop.copy()
						emit(Data(diary = diaryResult.data, crop = crop))
					}
					else -> throw CropLoadFailed(cropId)
				}
			})
		}
	}

	public fun new(): LiveData<Data>
	{
		isNew = true
		cropId.clear()

		viewModelScope.launch {
			diaryId.value?.let { diaryId ->
				val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed(diaryId)
				val count = diary.crops.size + 1
				val crop = Crop(
					name = "Crop $count",
					genetics = "Unknown genetics"
				)

				diariesRepository.addCrop(crop, diary)
				cropId.postValue(crop.id)
			}
		}

		return crop
	}

	public fun load(id: String): LiveData<Data>
	{
		isNew = false
		cropId.postValue(id)
		return crop
	}

	public fun remove()
	{
		viewModelScope.launch {
			cropId.clear()?.let { id ->
				val diaryId = diaryId.value ?: return@let
				val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed(diaryId)
				diariesRepository.removeCrop(id, diary)
			}
		}
	}

	public fun save(new: Crop)
	{
		viewModelScope.launch {
			val diaryId = diaryId.value ?: return@launch
			val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed(diaryId)
			diariesRepository.addCrop(new, diary)
		}
	}

	public fun clear()
	{
		isNew = false
		cropId.clear()
		crop.clear()
	}
}
