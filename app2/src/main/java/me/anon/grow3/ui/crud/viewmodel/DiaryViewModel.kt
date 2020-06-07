package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.*
import me.anon.grow3.util.states.asSuccess
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

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

	private var _diaryId: MutableLiveData<String?> = savedStateHandle.getLiveData("diary_id", null)
	private var _environmentId: MutableLiveData<String?> = savedStateHandle.getLiveData("environment_id", null)

	private val _diary = _diaryId.switchMap { id ->
		liveData {
			id ?: let {
				val count = diariesRepository.getDiaries().size
				val diary = diariesRepository.createDiary(Diary(name = "Gen ${count + 1}").apply {
					isDraft = true
					crops.add(Crop(
						name = "Crop 1",
						genetics = "Unknown genetics",
						platedDate = this@apply.date
					))
				})

				_diaryId.value = diary.id
				return@liveData
			}

			id?.let {
				emitSource(diariesRepository.observeDiary(it))
			}
		}
	} as MutableLiveData

	public val diary = _diary.asLiveData()

	public fun setDiaryDate(dateTime: ZonedDateTime)
	{
		_diary.value?.asSuccess()!!.apply {
			date = dateTime.asString()
			_diary.notifyChange()
		}
	}

	public fun setDiaryName(newName: String)
	{
		_diary.value?.apply {
			//name = newName
			_diary.postValue(this)
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
		_diary.value?.asSuccess()!!.apply {
			// We're in a wizard so there should only be one instance
			val id = _environmentId.value ?: let {
				log.add(Environment())
				log.last().id
			}.also { _environmentId.value = it }

			logOf<Environment>(id)?.apply {
				type?.applyValue { this.type = it }
				temperature?.applyValue { this.temperature = it }
				humidity?.applyValue { this.humidity = it }
				relativeHumidity?.applyValue { this.relativeHumidity = it }
				size?.applyValue { this.size = it }
				light?.applyValue { this.light = it }
				schedule?.applyValue { this.schedule = it }
			}
		}
		_diary.notifyChange()
	}

	public fun addCrop(crop: Crop)
	{
		_diary.value?.asSuccess()!!.apply {
			crops.add(crop)
			_diary.notifyChange()
		}
	}

	public fun refresh()
	{
		_diaryId.postValue(_diaryId.value)
	}

	public fun save(diary: Diary)
	{
		diary.isDraft = false
		diariesRepository.sync()
	}
}
