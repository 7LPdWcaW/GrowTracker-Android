package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.diaries.adapter.DiaryListAdapter
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

	sealed class UiResult
	{
		data class Loaded(val diaries: List<DiaryListAdapter.DiaryStub>) : UiResult()
		object Loading : UiResult()
	}

	public val state: Flow<UiResult> get() = flow {
		emitAll(diariesRepository.flowDiaries()
			.mapLatest {
				val data = (it as? DataResult.Success)?.data ?: throw GrowTrackerException.DiaryLoadFailed()
				UiResult.Loaded(data.map {
					DiaryListAdapter.DiaryStub(it.id, it.name)
				})
			})
	}
}
