package me.anon.view.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.anon.data.repository.PlantsRepository
import me.anon.model.Plant
import me.anon.model.PlantStage

/**
 * // TODO: Add class description
 */
class PlantListViewModel(
	private val plantsRepository: PlantsRepository
) : ViewModel()
{
	inner class Filter(
		val stage: PlantStage
	)

	public val allFilters = arrayListOf<Filter>().apply {
		addAll(PlantStage.values().map { Filter(it) })
	}

	public val filters = arrayListOf<Filter>().apply {
		addAll(allFilters)
	}

	private val _loaded = MutableLiveData<Boolean>(false)

	private val _forceUpdate = MutableLiveData(true)

	private val _plants: LiveData<List<Plant>> = _forceUpdate.switchMap { forceUpdate ->
		if (forceUpdate)
		{
			viewModelScope.launch {
				plantsRepository.reload()
				_loaded.postValue(true)
			}
		}

		plantsRepository.plants.switchMap { filterPlants(it) }
	}

	public val plants: LiveData<List<Plant>> = _plants
	public val loaded: LiveData<Boolean> = _loaded

	init {
		_forceUpdate.postValue(true)
	}

	public fun applyFilter(filter: Filter)
	{
		filters.clear()
		filters.addAll(arrayListOf(filter).apply {
			addAll(filters ?: arrayListOf())
		})
		_forceUpdate.value = true
	}

	public fun removeFilter(filter: Filter)
	{
		val newFilters = filters.filterNot { it == filter }
		filters.clear()
		filters.addAll(newFilters)
		_forceUpdate.value = true
	}

	private fun filterPlants(plants: List<Plant>): LiveData<List<Plant>>
	{
		val liveData = MutableLiveData<List<Plant>>()
		val filteredPlants = arrayListOf<Plant>()

		viewModelScope.launch {
			for (plant in plants)
			{
				if (filters.any { it.stage == plant.stage }) filteredPlants.add(plant)
			}

			liveData.value = filteredPlants
		}

		return liveData
	}
}
