package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.source.CacheDataSource
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.asString
import me.anon.grow3.util.notifyChange
import me.anon.grow3.util.states.DataResult
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class DiaryViewModel(
	private val diariesRepository: DiariesRepository,
	private val cacheRepository: CacheDataSource,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository,
		private val cacheRepository: CacheDataSource
	) : ViewModelFactory<DiaryViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryViewModel =
			DiaryViewModel(diariesRepository, cacheRepository, handle)
	}

	private var diaryId: String? = savedStateHandle.get(Extras.EXTRA_DIARY_ID)
	private var _environmentId: MutableLiveData<String?> = savedStateHandle.getLiveData("environment_id", null)

	public val diary = liveData {
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
			diaryId = cacheRepository.cache(diary)
			emit(diary)
			return@liveData
		}
		else
		{
			emitSource(diariesRepository.observeDiary(diaryId!!).map { diaryResult ->
				when (diaryResult)
				{
					is DataResult.Success -> diaryResult.data
					else -> throw DiaryLoadFailed(diaryId!!)
				}
			})
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
		diary.value!!.apply {
			// We're in a wizard so there should only be one instance
			val id = _environmentId.value ?: let {
				val log = Environment()
				viewModelScope.launch { diariesRepository.addLog(log, it) }
				log.id
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
		(diary as MutableLiveData).notifyChange()
	}

	public fun addCrop(crop: Crop)
	{
		diary.value!!.apply {
			crops as ArrayList += crop
			(diary as MutableLiveData).notifyChange()
		}
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
