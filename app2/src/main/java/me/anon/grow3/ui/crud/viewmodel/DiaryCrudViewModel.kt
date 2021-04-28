package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ViewModelFactory
import javax.inject.Inject

class DiaryCrudViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<DiaryCrudViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryCrudViewModel =
			DiaryCrudViewModel(diariesRepository, handle)
	}

	public val diaryVm = DiaryViewModel(diariesRepository, savedStateHandle, viewModelScope)
	public val cropVm = CropUseCase(diariesRepository, savedStateHandle, viewModelScope)

	public fun completeCrud()
	{
		val diary = (diaryVm.state.value as? DiaryViewModel.UiResult.Loaded)?.diary ?: return
		viewModelScope.launch {
			diary.isDraft = false
			diaryVm.save(diary)
		}
	}
}
