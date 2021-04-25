package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.data.exceptions.GrowTrackerException.*
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

	public val diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: throw InvalidDiaryId()

	private val _diary = diariesRepository.observeDiary(diaryId)
	public val diary: LiveData<DataResult<Diary>> = _diary
}
