package me.anon.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.anon.data.repository.GardensRepository
import me.anon.grow.R
import me.anon.model.Garden

/**
 * // TODO: Add class description
 */
class MainViewModel(
	private val gardenRepository: GardensRepository
) : ViewModel()
{
	private val _gardens = gardenRepository.observeGardens()
	public val gardens: LiveData<List<Garden>> = _gardens

	public val selectedPage: MutableLiveData<Int> = MutableLiveData(R.id.all)

	public fun setSelectedPage(id: Int)
	{
		selectedPage.postValue(id)
	}

	public fun start()
	{
		viewModelScope.launch {
			gardenRepository.getGardens()
		}
	}
}
