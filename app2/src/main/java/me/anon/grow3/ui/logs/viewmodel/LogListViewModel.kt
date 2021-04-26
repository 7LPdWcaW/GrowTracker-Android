package me.anon.grow3.ui.logs.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
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

	public val diaryId = savedState.getLiveData<String>(EXTRA_DIARY_ID)
	private val cropIds = MutableLiveData<List<String>>()

	public val diary: LiveData<DataResult<Diary>> = diaryId.switchMap { diariesRepository.observeDiary(it) }
	public val logs = diary.switchMap { diaryResult ->
		when (diaryResult)
		{
			is DataResult.Success -> {
				cropIds.switchMap { cropIds ->
					MutableLiveData(
						diaryResult.data.log.filter {
							if (cropIds.isEmpty()) true
							else it.cropIds.containsAll(cropIds)
						}
					)
				}
			}
			else -> throw DiaryLoadFailed(diaryId.value!!)
		}
	}

	public fun load(diaryId: String, filterCrops: List<String> = arrayListOf())
	{
		this.diaryId.postValue(diaryId)
		this.cropIds.postValue(filterCrops)
	}
}
