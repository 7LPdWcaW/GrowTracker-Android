package me.anon.view.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.anon.data.repository.PlantsRepository
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
class MainViewModel(
	private val plantsRepository: PlantsRepository
) : ViewModel()
{
	private val _forceUpdate = MutableLiveData<Boolean>(false)

	private val _plants: LiveData<List<Plant>> = _forceUpdate.switchMap { forceUpdate ->
		if (forceUpdate)
		{
			viewModelScope.launch {
				plantsRepository.reload()
			}
		}

		plantsRepository.observePlants()
	}

	public val plants: LiveData<List<Plant>> = _plants

	init {
		_forceUpdate.postValue(true)
	}
}
