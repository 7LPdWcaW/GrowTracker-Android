package me.anon.grow3.ui.diaries.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class LogListViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<LogListViewModel>
	{
		override fun create(handle: SavedStateHandle): LogListViewModel =
			LogListViewModel(diariesRepository, handle)
	}

	public val diaryId: String = savedState[EXTRA_DIARY_ID] ?: throw kotlin.IllegalArgumentException("No diary id set")
	private val cropIds = MutableLiveData<ArrayList<String>>()

	private val _diary = diariesRepository.observeDiary(diaryId)
	public val diary: LiveData<DataResult<Diary>> = _diary

	public val logs = cropIds.switchMap { cropIds ->
		diary.switchMap { diaryResult ->
			when (diaryResult)
			{
				is DataResult.Success -> {
					MutableLiveData(
						diaryResult.data.log.filter {
							if (cropIds.isEmpty()) true
							else it.cropIds.containsAll(cropIds)
						}
					)
				}
				else -> throw IllegalStateException("Could not load diary")
			}
		}
	}

	init {
		cropIds.postValue(ArrayList(savedState.get<Array<String>?>(Extras.EXTRA_CROP_IDS)?.asList() ?: arrayListOf()))
	}
}
