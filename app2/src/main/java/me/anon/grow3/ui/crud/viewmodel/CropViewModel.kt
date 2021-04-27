package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import com.zhuinden.livedatacombinetuplekt.combineTuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.CropLoadFailed
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.clear
import me.anon.grow3.util.states.Data
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.toStringOrNull

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

	private var originalCrop: Crop? = null
	private val diaryId: MutableLiveData<String> = savedStateHandle.getLiveData(Extras.EXTRA_DIARY_ID)
	private val cropId: MutableLiveData<String> = savedStateHandle.getLiveData(Extras.EXTRA_CROP_ID)
	public val crop: LiveData<Data> = combineTuple(diaryId, cropId).switchMap { (diaryId, cropId) ->
		liveData<Data> {
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

				if (diary.stageOf(crop) == null)
				{
					// add default stage
					diariesRepository.addLog(StageChange(StageType.Planted).apply {
						date = crop.platedDate
						cropIds += crop.id
					}, diary)
				}

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

	public fun setCrop(
		name: ValueHolder<String>? = null,
		genetics: ValueHolder<String?>? = null,
		numberOfPlants: ValueHolder<Int>? = null,
		mediumType: ValueHolder<MediumType>? = null,
		volume: ValueHolder<Volume?>? = null
	)
	{
		val diary = crop.value?.diary ?: return
		val crop = crop.value?.crop ?: return

		viewModelScope.launch {
			val newCrop = crop.apply {
				name?.applyValue { this.name = it }
				genetics?.applyValue { this.genetics = it.toStringOrNull() }
				numberOfPlants?.applyValue { this.numberOfPlants = it }

				// medium - only 1 medium type to set
				val medium = diary.mediumOf(this)
					?: let {
						mediumType?.let { type ->
							Medium(type.value).also {
								diariesRepository.addLog(it, diary)
							}
						}
					}

				medium?.apply {
					mediumType?.applyValue { this.medium = it }
					volume?.applyValue { this.size = it }
					diariesRepository.addLog(this, diary)
				}
			}

			save(newCrop)
		}
	}

	public fun clear()
	{
		isNew = false
		cropId.clear()
		crop.clear()
	}
}
