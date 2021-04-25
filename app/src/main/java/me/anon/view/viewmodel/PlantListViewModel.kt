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

	private val _filters = MutableLiveData<ArrayList<Filter>>(arrayListOf<Filter>().apply {
		addAll(allFilters)
	})
	public val filters: LiveData<ArrayList<Filter>> = _filters

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

		plantsRepository.plants
	}

	public val plants: LiveData<List<Plant>> = _plants
	public val loaded: LiveData<Boolean> = _loaded

	init {
		_forceUpdate.postValue(true)
	}

	public fun applyFilter(filter: Filter)
	{
		_filters.value = arrayListOf(filter).apply {
			addAll(_filters.value ?: arrayListOf())
		}
		_forceUpdate.value = true
	}

	public fun removeFilter(filter: Filter)
	{
		val newFilters = filters.value?.filterNot { it == filter } as ArrayList?
		_filters.value = newFilters ?: allFilters
		_forceUpdate.value = true
	}
}
