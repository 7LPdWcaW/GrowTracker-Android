package me.anon.view.viewmodel

import androidx.lifecycle.*
import me.anon.data.repository.PlantsRepository
import me.anon.model.Plant
import me.anon.model.Water

/**
 * // TODO: Add class description
 */
class WateringViewModel(
	public val plantsRepository: PlantsRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	public var plantId: String? = null
		set(value)
		{
			field = value
			savedStateHandle["plantId"] = value
		}

	public var actionId: String? = null
		set(value)
		{
			field = value
			savedStateHandle["actionId"] = value
		}

	private val _plant = MutableLiveData<Plant>()
	public val plant = plantsRepository.observePlants().switchMap { plants ->
		_plant.value = plants.first { it.id == plantId }
		_plant
	}

	private val _action = MutableLiveData<Water>()
	public val action: LiveData<Water> = plant.switchMap { plant ->
		(plant.actions.firstOrNull { it.id == actionId } as? Water)?.let {
			_action.value = it
		}

		_action
	}

	init {
		plantId = savedStateHandle["plantId"] ?: plantId
		actionId = savedStateHandle["actionId"] ?: actionId
	}

	public fun initialise()
	{

	}
}
