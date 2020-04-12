package me.anon.view.viewmodel

import androidx.lifecycle.*
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
	public val plant = plantsRepository.observePlants().switchMap { plants ->
		_plant.value = plants.find { it.id == plantId }
		_plant
	}

	public val name = MutableLiveData<String>()
	public val strain = MutableLiveData<String>()

	init {
		plantId = savedStateHandle["plantId"] ?: plantId
	}

	public fun initialise()
	{

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
