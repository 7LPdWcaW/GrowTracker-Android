package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.states.DataResult

class DiaryUseCase(
	private val diariesRepository: DiariesRepository
)
{
	private var diary: Diary? = null
	public var isNew: Boolean = false; private set

	public fun cached(): Diary = diary ?: throw GrowTrackerException.IllegalState("Diary was null")

	public suspend fun new(): Flow<Diary>
	{
		isNew = true
		val count = diariesRepository.getDiaries().filter { !it.isDraft }.size
		val newDiary = Diary(name = "Gen ${count + 1}").apply {
			isDraft = true
			crops as ArrayList += Crop(
				name = "Crop 1",
				genetics = "Unknown genetics",
				platedDate = this@apply.date
			)
		}

		diary = diariesRepository.addDiary(newDiary)
		return diariesRepository.flowDiary(newDiary.id)
			.map { result ->
				when (result)
				{
					is DataResult.Success -> result.data
					else -> throw GrowTrackerException.DiaryLoadFailed(newDiary.id)
				}
			}
	}

	public fun load(id: String)
	{
//		isNew = false
//		viewModelScope.launch {
//			diariesRepository.flowDiary(id)
//				.map { result ->
//					when (result)
//					{
//						is DataResult.Success -> UiResult.Loaded(result.data, isNew)
//						else -> throw GrowTrackerException.DiaryLoadFailed(id)
//					}
//				}
//				.collect {
//					diaryId = it.diary.id
//					state.emit(it)
//				}
//		}
	}

	public fun remove()
	{
		//diariesRepository.deleteDiary(id)
	}

	public suspend fun save(new: Diary)
	{
		this.diary = diariesRepository.addDiary(new)
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
//		val diary = (state.value as? UiResult.Loaded)?.diary ?: return
//
//		viewModelScope.launch {
//			// We're in a wizard so there should only be one instance
//			val environment: Environment = diary.environment()
//				?: Environment().apply {
//					diariesRepository.addLog(this, diary)
//				}
//
//			environment.apply {
//				type?.applyValue { this.type = it }
//				temperature?.applyValue { this.temperature = it }
//				humidity?.applyValue { this.humidity = it }
//				relativeHumidity?.applyValue { this.relativeHumidity = it }
//				size?.applyValue { this.size = it }
//				light?.applyValue { this.light = it }
//				schedule?.applyValue { this.schedule = it }
//			}
//
//			diary.log(environment)
//			save(diary)
//		}
	}

	public fun clear()
	{
//		isNew = false
//		diaryId = ""
		//state.clear()
	}
}
