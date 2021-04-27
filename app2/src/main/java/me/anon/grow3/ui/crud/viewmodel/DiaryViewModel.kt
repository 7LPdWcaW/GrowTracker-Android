package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.clear
import me.anon.grow3.util.states.DataResult

class DiaryViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle,
	private val viewModelScope: CoroutineScope
)
{
	public var isNew: Boolean = false
		get() = savedStateHandle["new_diary"] ?: false
		private set(value) {
			savedStateHandle["new_diary"] = value
			field = value
		}

	private val diaryId: MutableLiveData<String> = savedStateHandle.getLiveData(Extras.EXTRA_DIARY_ID)
	public val diary: LiveData<Diary> = diaryId.switchMap { id ->
		liveData<Diary> {
			if (id.isNullOrBlank()) return@liveData

			emitSource(diariesRepository.observeDiary(id).switchMap { diaryResult ->
				when (diaryResult)
				{
					is DataResult.Success -> liveData { emit(diaryResult.data) }
					else -> throw GrowTrackerException.DiaryLoadFailed(id)
				}
			})
		}
	}

	public fun new(): LiveData<Diary>
	{
		isNew = true
		diaryId.clear()

		viewModelScope.launch {
			val count = diariesRepository.getDiaries().filter { !it.isDraft }.size
			val diary = Diary(name = "Gen ${count + 1}").apply {
				isDraft = true
				crops as ArrayList += Crop(
					name = "Crop 1",
					genetics = "Unknown genetics",
					platedDate = this@apply.date
				)
			}

			diariesRepository.addDiary(diary)
			diaryId.postValue(diary.id)
		}

		return diary
	}

	public fun load(id: String): LiveData<Diary>
	{
		isNew = false
		diaryId.postValue(id)
		return diary
	}

	public fun remove()
	{
		viewModelScope.launch {
			diaryId.clear()?.let { id ->
				diariesRepository.deleteDiary(id)
			}
		}
	}

	public fun save(new: Diary = diary.value!!)
	{
		viewModelScope.launch {
			diariesRepository.addDiary(new)
		}
	}

	public fun mutate(block: (Diary) -> Diary)
	{
		diary.value?.let {
			save(block(it))
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
		val diary = diary.value ?: return

		viewModelScope.launch {
			// We're in a wizard so there should only be one instance
			val environment: Environment = diary.environment()
				?: Environment().apply {
					diariesRepository.addLog(this, diary)
				}

			environment.apply {
				type?.applyValue { this.type = it }
				temperature?.applyValue { this.temperature = it }
				humidity?.applyValue { this.humidity = it }
				relativeHumidity?.applyValue { this.relativeHumidity = it }
				size?.applyValue { this.size = it }
				light?.applyValue { this.light = it }
				schedule?.applyValue { this.schedule = it }
			}

			diary.log(environment)
			save(diary)
		}
	}

	public fun clear()
	{
		isNew = false
		diaryId.clear()
		diary.clear()
	}
}
