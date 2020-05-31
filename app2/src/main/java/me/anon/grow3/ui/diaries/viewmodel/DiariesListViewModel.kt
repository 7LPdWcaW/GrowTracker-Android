package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class DiariesListViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<DiariesListViewModel>
	{
		override fun create(handle: SavedStateHandle): DiariesListViewModel =
			DiariesListViewModel(diariesRepository, handle)
	}

	private val _gardens = diariesRepository.observeDiaries()
	public val gardens: LiveData<DataResult<List<Diary>>> = _gardens
}
