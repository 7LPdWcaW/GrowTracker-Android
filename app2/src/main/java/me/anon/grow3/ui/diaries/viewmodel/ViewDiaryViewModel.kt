package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.flow.collect
import me.anon.grow3.data.exceptions.GrowTrackerException.InvalidDiaryId
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class ViewDiaryViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<ViewDiaryViewModel>
	{
		override fun create(handle: SavedStateHandle): ViewDiaryViewModel =
			ViewDiaryViewModel(diariesRepository, handle)
	}

	sealed class UiResult
	{
		data class Loaded(val diary: Diary): UiResult()
		object Error : UiResult()
	}

	public val diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: throw InvalidDiaryId()
	public val diary: LiveData<UiResult> get()
		= liveData {
			diariesRepository.flowDiary(diaryId)
				.collect {
					when (it)
					{
						is DataResult.Success -> emit(UiResult.Loaded(it.data))
						else -> emit(UiResult.Error)
					}
				}
		}
}
