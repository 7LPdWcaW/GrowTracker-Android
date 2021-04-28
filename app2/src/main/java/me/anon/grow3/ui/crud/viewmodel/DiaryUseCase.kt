package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.states.DataResult

class DiaryUseCase(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle,
	private val viewModelScope: CoroutineScope
)
{
	sealed class UiResult
	{
		data class Loaded(val diary: Diary, val isNew: Boolean = false) : UiResult()
		object Loading : UiResult()
	}

	private var isNew: Boolean = false
		get() = savedStateHandle["new_diary"] ?: false
		private set(value) {
			savedStateHandle["new_diary"] = value
			field = value
		}

	public var diaryId: String = savedStateHandle.get<String>(Extras.EXTRA_DIARY_ID) ?: ""
	public val state: MutableStateFlow<UiResult> = MutableStateFlow(UiResult.Loading)

	public fun new()
	{
		isNew = true
		//diaryId.clear()

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
			diariesRepository.flowDiary(diary.id)
				.map { result ->
					when (result)
					{
						is DataResult.Success -> UiResult.Loaded(result.data, isNew)
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
		viewModelScope.launch {
			diariesRepository.flowDiary(id)
				.map { result ->
					when (result)
					{
						is DataResult.Success -> UiResult.Loaded(result.data, isNew)
						else -> throw GrowTrackerException.DiaryLoadFailed(id)
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
			diaryId.let { id ->
				diariesRepository.deleteDiary(id)
			}
		}
	}

	public fun save(new: Diary)
	{
		viewModelScope.launch {
			diariesRepository.addDiary(new)
		}
	}

	public fun mutate(block: (Diary) -> Diary)
	{
		val diary = (state.value as? UiResult.Loaded)?.diary ?: return
		save(block(diary))
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
		val diary = (state.value as? UiResult.Loaded)?.diary ?: return

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
		diaryId = ""
		//state.clear()
	}
}
