package me.anon.view.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.async
import me.anon.data.repository.PlantsRepository
import me.anon.lib.ext.addOrUpdate
import me.anon.model.Plant
import me.anon.model.Tds
import me.anon.model.Water

/**
 * // TODO: Add class description
 */
class WateringViewModel(
	private val plantsRepository: PlantsRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	private var _plantId: String? = null
	private var _actionId: String? = null

	public var plant: LiveData<Plant> = liveData {
		emit(plantsRepository.getPlantById(_plantId!!) ?: throw IllegalArgumentException("Plant not found"))
	}

	public val action: LiveData<Water> = liveData {
		(plant.value!!.actions.firstOrNull { it.id == _actionId } as? Water)?.let {
			emit(it)
		}
	}

	public fun initialise(plantId: String, actionId: String?)
	{
		_plantId = plantId.also { savedStateHandle["plantId"] = it }
		_actionId = actionId.also { savedStateHandle["actionId"] = it }
	}

	public fun setValues(ph: Double?, tds: Double?)
	{
		val action = action.value ?: Water()
		action.ph = ph
		tds?.let { action.tds = Tds(it) }

		plant.value!!.actions.addOrUpdate(action) { action.id }

		viewModelScope.async {
			plantsRepository.save()
		}
	}
}
