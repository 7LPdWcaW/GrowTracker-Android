package me.anon.view.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.anon.data.repository.PlantsRepository
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
class PlantListViewModel(
	private val plantsRepository: PlantsRepository
) : ViewModel()
{
	private val _loaded = MutableLiveData<Boolean>(false)
	private val _forceUpdate = MutableLiveData<Boolean>(false)

	private val _plants: LiveData<List<Plant>> = _forceUpdate.switchMap { forceUpdate ->
		if (forceUpdate)
		{
			viewModelScope.launch {
				plantsRepository.reload()
				_loaded.postValue(true)
			}
		}

		plantsRepository.observePlants()
	}

	public val plants: LiveData<List<Plant>> = _plants
	public val loaded: LiveData<Boolean> = _loaded

	init {
		_forceUpdate.postValue(true)
	}
}
