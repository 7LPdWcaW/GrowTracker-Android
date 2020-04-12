package me.anon.view.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.anon.data.repository.GardensRepository
import me.anon.data.repository.PlantsRepository

/**
 * // TODO: Add class description
 */
class BootViewModel(
	private val plantsRepository: PlantsRepository,
	private val gardensRepository: GardensRepository,
	application: Application
) : AndroidViewModel(application)
{


	public val plantsLoaded: LiveData<Result<Boolean>> = plantsRepository.loaded()
	public val gardensLoaded: LiveData<Result<Boolean>> = gardensRepository.loaded()

	public fun initialise()
	{
		viewModelScope.launch {
			launch { plantsRepository.getPlants() }
			launch { gardensRepository.getGardens() }
		}
	}
}
