package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.toStringOrNull
import javax.inject.Inject

class DiaryCrudViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<DiaryCrudViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryCrudViewModel =
			DiaryCrudViewModel(diariesRepository, handle)
	}

	public val diaryVm = DiaryViewModel(diariesRepository, savedStateHandle, viewModelScope)
	public val cropVm = CropViewModel(diariesRepository, savedStateHandle, viewModelScope)

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

	public fun setCrop(
		name: ValueHolder<String>? = null,
		genetics: ValueHolder<String?>? = null,
		numberOfPlants: ValueHolder<Int>? = null,
		mediumType: ValueHolder<MediumType>? = null,
		volume: ValueHolder<Double?>? = null
	)
	{
		val crop = cropVm.crop.value?.crop ?: return
		val diary = diaryVm.diary.value ?: return

		val newCrop = crop.apply {
			name?.applyValue { this.name = it }
			genetics?.applyValue { this.genetics = it.toStringOrNull() }
			numberOfPlants?.applyValue { this.numberOfPlants = it }

			// medium - only 1 medium type to set
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

		cropVm.save(newCrop)
	}
}
