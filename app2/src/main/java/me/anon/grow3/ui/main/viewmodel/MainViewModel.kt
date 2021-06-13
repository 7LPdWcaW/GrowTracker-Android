package me.anon.grow3.ui.main.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ViewModelFactory
import javax.inject.Inject

class MainViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<MainViewModel>
	{
		override fun create(handle: SavedStateHandle): MainViewModel =
			MainViewModel(diariesRepository, handle)
	}

	public val logEvents = diariesRepository.flowLogEvents()
}
