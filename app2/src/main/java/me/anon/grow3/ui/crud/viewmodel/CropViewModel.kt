package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Medium
import me.anon.grow3.data.model.MediumType
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras.EXTRA_CROP_ID
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
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

	public var newCrop = savedStateHandle.get("new_crop") ?: false
		set(value) { field = value.apply { savedStateHandle["new_crop"] = this } }
	private var cropComparator = savedStateHandle.get("crop_str") ?: ""
		set(value) { field = value.apply { savedStateHandle["crop_str"] = this } }

	private val _diaryId: String = savedStateHandle.get(EXTRA_DIARY_ID)!!
	private var _cropId: String? = savedStateHandle.get(EXTRA_CROP_ID)

	public val diary = diariesRepository.observeDiary(_diaryId)

	private val _crop = diary.switchMap { diary ->
		if (!diary.isSuccess) throw IllegalArgumentException("Unable to load diary")
		val diary = diary.asSuccess()

		liveData {
			if (_cropId.isNullOrBlank())
			{
				val crop = Crop(name = "", genetics = "")
				diary.crops += crop
				newCrop = true
				_cropId = crop.id
				cropComparator = crop.toJsonString()
				diariesRepository.sync()
				return@liveData
			}

			emit(diary.crop(_cropId!!).also {
				cropComparator = it.toJsonString()
			})
		}
	}.asMutableLiveData()

	public val crop = _crop.asLiveData()

	public fun setCrop(
		name: ValueHolder<String>? = null,
		genetics: ValueHolder<String?>? = null,
		numberOfPlants: ValueHolder<Int>? = null,
		mediumType: ValueHolder<MediumType>? = null,
		volume: ValueHolder<Double?>? = null
	)
	{
		crop.value?.apply {
			name?.applyValue { this.name = it }
			genetics?.applyValue { this.genetics = it.toStringOrNull() }
			numberOfPlants?.applyValue { this.numberOfPlants = it }

			diary.value?.asSuccess()?.let { diary ->
				val medium = diary.mediumOf(this) ?: let {
					mediumType?.let {
						Medium(it.value).also {
							diary.log += it
						}
					}
				}

				medium?.apply {
					mediumType?.applyValue { this.medium = it }
					volume?.applyValue { this.size = it }
				}
			}
		}
		_crop.notifyChange()
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
				diary.crops.removeAll { it.id == _cropId }
			}
		}
	}

	public fun save()
	{
		diariesRepository.sync()
	}
}
