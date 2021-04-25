package me.anon.view.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
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
	private lateinit var preferences: SharedPreferences

	public val plantsLoaded: LiveData<Result<Boolean>> = plantsRepository.loaded()
	public val gardensLoaded: LiveData<Result<Boolean>> = gardensRepository.loaded()

	public fun initialise()
	{
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

		viewModelScope.launch {
			launch { plantsRepository.getPlants() }
			launch { gardensRepository.getGardens() }
		}
	}
}
