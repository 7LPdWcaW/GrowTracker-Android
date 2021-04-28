package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class DiaryListViewModel constructor(
	private val diariesRepository: DiariesRepository,
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository,
	) : ViewModelFactory<DiaryListViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryListViewModel =
			DiaryListViewModel(diariesRepository)
	}

	private val _diaries = diariesRepository.flowDiaries()
	public val diaries: LiveData<DataResult<List<Diary>>> = _diaries.asLiveData(viewModelScope.coroutineContext)
}
