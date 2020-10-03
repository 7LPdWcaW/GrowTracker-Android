package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import com.zhuinden.livedatacombinetuplekt.combineTuple
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.CropLoadFailed
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.*
import me.anon.grow3.util.states.DataResult
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

/**
 * why are we saving to the cache repository? just save to the diaries and remove it if the user cancels the process?
 * cant pass diary with new crop because crop doesnt exist in diary so log action doesnt show it
 */
class DiaryViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<DiaryViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryViewModel =
			DiaryViewModel(diariesRepository, handle)
	}

	private var diaryId: String? = savedStateHandle.get(Extras.EXTRA_DIARY_ID)
	private var _environmentId: MutableLiveData<String?> = savedStateHandle.getLiveData("environment_id", null)
	private var cropId: LiveData<String> = savedStateHandle.getLiveData(Extras.EXTRA_CROP_ID)

	public val diary: LiveData<Diary> = liveData {
		if (diaryId.isNullOrBlank())
		{
			val count = diariesRepository.getDiaries().size
			val diary = Diary(name = "Gen ${count + 1}").apply {
				isDraft = true
				crops as ArrayList += Crop(
					name = "Crop 1",
					genetics = "Unknown genetics",
					platedDate = this@apply.date
				)
			}

			diaryId = diariesRepository.addDiary(diary).id
		}

		emitSource(diariesRepository.observeDiary(diaryId!!).switchMap { diaryResult ->
			liveData {
				when (diaryResult)
				{
					is DataResult.Success -> emit(diaryResult.data)
					else -> throw DiaryLoadFailed(diaryId!!)
				}
			}
		})
	}

	public var newCrop: Boolean = false
		get() = savedStateHandle["new_crop"] ?: false
		set(value) {
			savedStateHandle["new_crop"] = value
			field = value
		}

	private val originalCrop: MutableLiveData<Crop> = MutableLiveData<Crop>(null)
	public val crop: LiveData<Crop> = combineTuple(cropId, diary).switchMap { (id, diary) ->
		liveData {
			if (id.isNullOrBlank()) return@liveData
			requireNotNull(diary)

			val crop = diariesRepository.getCrop(id, diary) ?: throw CropLoadFailed(id, diary.id)

			if (originalCrop.value == null)
			{
				originalCrop.value = crop.copy()
			}

			emit(crop)
		}
	}

	public fun setDiaryDate(dateTime: ZonedDateTime)
	{
		diary.value!!.apply {
			date = dateTime.asString()
			(diary as MutableLiveData).notifyChange()
		}
	}

	public fun setDiaryName(newName: String)
	{
		diary.value!!.apply {
			//name = newName
			(diary as MutableLiveData).notifyChange()
		}
	}

	public fun setEnvironment(
		type: ValueHolder<EnvironmentType?>? = null,
		temperature: ValueHolder<Double?>? = null,
		humidity: ValueHolder<Double?>? = null,
		relativeHumidity: ValueHolder<Double?>? = null,
		size: ValueHolder<Size?>? = null,
		light: ValueHolder<Light?>? = null,
		schedule: ValueHolder<LightSchedule?>? = null
	)
	{
//		diary.value!!.apply {
//			// We're in a wizard so there should only be one instance
//			val id = _environmentId.value ?: let {
//				val log = Environment()
//				viewModelScope.launch { diariesRepository.addLog(log, it) }
//				log.id
//			}.also { _environmentId.value = it }
//
//			logOf<Environment>(id)?.apply {
//				type?.applyValue { this.type = it }
//				temperature?.applyValue { this.temperature = it }
//				humidity?.applyValue { this.humidity = it }
//				relativeHumidity?.applyValue { this.relativeHumidity = it }
//				size?.applyValue { this.size = it }
//				light?.applyValue { this.light = it }
//				schedule?.applyValue { this.schedule = it }
//			}
//		}
		//(diary as MutableLiveData).notifyChange()
	}

	public fun newCrop()
	{
		newCrop = true
		(cropId as MutableLiveData).value = null

		viewModelScope.launch {
			val crop = diariesRepository.addCrop(Crop(name = "Crop " + (diary.value!!.crops.size + 1)), diary.value!!)
			(cropId as MutableLiveData).postValue(crop.id)
		}
	}

	public fun editCrop(cropId: String)
	{
		newCrop = false
		(this.cropId as MutableLiveData).postValue(cropId)
	}

	public fun saveCrop(newCrop: Crop)
	{
//		viewModelScope.launch {
//			diariesRepository.addCrop(newCrop, diary.value!!)
//			(cropId as MutableLiveData).postValue(null)
//		}
	}

	public fun removeCrop()
	{
		viewModelScope.launch {
			requireNotNull(diary.value)
			requireNotNull(cropId.value)

			if (newCrop)
			{
				val id = cropId.value
				(cropId as MutableLiveData).value = null
				diariesRepository.removeCrop(id!!, diary.value!!)
			}
		}
	}

	public fun setCrop(
		name: ValueHolder<String>? = null,
		genetics: ValueHolder<String?>? = null,
		numberOfPlants: ValueHolder<Int>? = null,
		mediumType: ValueHolder<MediumType>? = null,
		volume: ValueHolder<Double?>? = null
	)
	{
		cropId.value ?: return

		val newCrop = crop.value!!.apply {
			name?.applyValue { this.name = it }
			genetics?.applyValue { this.genetics = it.toStringOrNull() }
			numberOfPlants?.applyValue { this.numberOfPlants = it }

			// medium - only 1 medium type to set
			diary.value?.let { diary ->
				val medium = diary.mediumOf(this) ?: let {
					mediumType?.let {
						Medium(it.value).also {
							viewModelScope.launch {
								diariesRepository.addLog(it, diary)
							}
						}
					}
				}

				medium?.apply {
					mediumType?.applyValue { this.medium = it }
					volume?.applyValue { this.size = it }
				}
			}
		}

		viewModelScope.launch {
			diariesRepository.addCrop(newCrop, diary.value!!)
		}
		//(crop as MutableLiveData).notifyChange()
	}

	public fun refresh()
	{
		//_diaryId.postValue(_diaryId.value)
	}

	public fun save()
	{

		//diariesRepository.addDiary(diary)
		diariesRepository.sync()
	}
}
