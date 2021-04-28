package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.toStringOrNull

class CropUseCase(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle,
	private val viewModelScope: CoroutineScope
)
{
	sealed class UiResult
	{
		data class Loaded(val diary: Diary, val crop: Crop, val isNew: Boolean = false) : UiResult()
		object Loading : UiResult()
	}

	public var isNew: Boolean = false
		get() = savedStateHandle["new_crop"] ?: false
		private set(value) {
			savedStateHandle["new_crop"] = value
			field = value
		}

	private var originalCrop: Crop? = null
	private var diaryId: String = savedStateHandle[Extras.EXTRA_DIARY_ID] ?: ""
	private var cropId: String = savedStateHandle[Extras.EXTRA_CROP_ID] ?: ""

	public val state: MutableStateFlow<UiResult> = MutableStateFlow(UiResult.Loading)

	public fun new()
	{
		isNew = true
		cropId = ""

		viewModelScope.launch {
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

			diariesRepository.flowDiary(diary.id)
				.map { result ->
					when (result)
					{
						is DataResult.Success ->
						{
							val crop = result.data.crop(cropId)
							originalCrop = crop.copy()
							UiResult.Loaded(result.data, crop, isNew)
						}
						else -> throw GrowTrackerException.DiaryLoadFailed(diary.id)
					}
				}
				.collect {
					diaryId = it.diary.id
					state.emit(it)
				}
		}
	}

	public fun load(id: String)
	{
		isNew = false
		cropId = id

		viewModelScope.launch {
			val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed(diaryId)
			diariesRepository.flowDiary(diary.id)
				.map { result ->
					when (result)
					{
						is DataResult.Success ->
						{
							val crop = result.data.crop(cropId)
							originalCrop = crop.copy()
							UiResult.Loaded(result.data, crop, isNew)
						}
						else -> throw DiaryLoadFailed(diary.id)
					}
				}
				.collect {
					diaryId = it.diary.id
					state.emit(it)
				}
		}
	}

	public fun remove()
	{
		viewModelScope.launch {
			cropId.let { id ->
				val diaryId = diaryId ?: return@let
				val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed(diaryId)
				diariesRepository.removeCrop(id, diary)
			}
		}
	}

	public fun save(new: Crop)
	{
		viewModelScope.launch {
			val diaryId = diaryId ?: return@launch
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
		val state = state.value as? UiResult.Loaded ?: return
		val diary = state.diary
		val crop = state.crop

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
		cropId = ""
	}
}
