package me.anon.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.anon.data.repository.PlantsRepository
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
class PlantDetailsViewModel(
	private val plantsRepository: PlantsRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	private var newPlant = true
	public var plantId: String? = null
		set(value)
		{
			field = value
			savedStateHandle["plantId"] = plantId
		}

	private val _plant: MutableLiveData<Plant> = MutableLiveData()
	public val plant = _plant

	public val name = MutableLiveData<String>()
	public val strain = MutableLiveData<String>()

	init {
		plantId = savedStateHandle["plantId"] ?: plantId
	}

	public fun initialise()
	{
		plantId?.let {
			viewModelScope.launch {
				plantsRepository.getPlants().firstOrNull { it.id == plantId }?.also { plant ->
					newPlant = false
					_plant.postValue(plant)
				}
			}
		}
	}

	public fun savePlant()
	{
		if (newPlant || plantId == null)
		{
			saveNewPlant(Plant(
				name = name.value ?: "",
				strain = strain.value!!
			))
		}
		else
		{
			savePlant(plant.value!!.also {
				it.name = name.value ?: ""
				it.strain = strain.value!!
			})
		}
	}

	private fun saveNewPlant(plant: Plant) = viewModelScope.launch {
		plantsRepository.addPlant(plant)
		plantsRepository.save()
	}

	private fun savePlant(plant: Plant) = viewModelScope.launch {
		plantsRepository.setPlant(plant)
		plantsRepository.save()
	}
}
