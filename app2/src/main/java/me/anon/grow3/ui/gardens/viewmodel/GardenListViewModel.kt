package me.anon.grow3.ui.gardens.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import me.anon.grow3.data.model.Garden
import me.anon.grow3.data.repository.GardensRepository
import me.anon.grow3.util.DataResult
import me.anon.grow3.util.ViewModelFactory
import javax.inject.Inject

/**
 * // TODO: Add class description
 */
class GardenListViewModel constructor(
	private val gardensRepository: GardensRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val gardensRepository: GardensRepository
	) : ViewModelFactory<GardenListViewModel>
	{
		override fun create(handle: SavedStateHandle): GardenListViewModel =
			GardenListViewModel(gardensRepository, handle)
	}

	private val _gardens = gardensRepository.observeGardens()
	public val gardens: LiveData<DataResult<List<Garden>>> = _gardens
}
