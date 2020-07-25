package me.anon.grow3.ui.crops.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class CropListViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<CropListViewModel>
	{
		override fun create(handle: SavedStateHandle): CropListViewModel =
			CropListViewModel(diariesRepository, handle)
	}

	public val diaryId: String = savedState[Extras.EXTRA_DIARY_ID] ?: throw kotlin.IllegalArgumentException("No diary id set")

	public val diary: LiveData<DataResult<Diary>> = diariesRepository.observeDiary(diaryId)
	public val crops: LiveData<List<Crop>> = diary.switchMap { dataResult ->
		when (dataResult)
		{
			is DataResult.Success -> {
				MutableLiveData(dataResult.data.crops)
			}
			else -> throw IllegalStateException("Failed to load diary")
		}
	}
}
