package me.anon.grow3.ui.logs.viewmodel

import androidx.lifecycle.*
import com.zhuinden.livedatacombinetuplekt.combineTuple
import me.anon.grow3.data.exceptions.GrowTrackerException.DiaryLoadFailed
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
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

	sealed class ViewData
	{
		data class Complete(val diary: Diary, val logs: List<Log>, val crops: List<Crop>? = null) : ViewData()
	}

	private val diaryId = savedState.getLiveData<String>(EXTRA_DIARY_ID)
	private val cropIds = MutableLiveData<List<String>>()

	public val data: LiveData<ViewData> = combineTuple(diaryId, cropIds).switchMap { (diaryId, cropIds) ->
		liveData {
			if (diaryId.isNullOrBlank()) return@liveData

			// should this react to changes on the diary?
			emitSource(diariesRepository.observeDiary(diaryId).switchMap { diaryResult ->
				if (diaryResult !is DataResult.Success) throw DiaryLoadFailed(diaryId)

				liveData<ViewData> {
					val diary = diaryResult.data
					val logs = diaryResult.data.log.filter {
						if (cropIds.isNullOrEmpty()) true
						else it.cropIds.containsAll(cropIds)
					}
					val crops = cropIds?.map { diary.crop(it) }

					emit(ViewData.Complete(diary = diary, logs = logs, crops = crops))
				}
			})
		}
	}

	public fun load(diaryId: String, filterCrops: List<String> = arrayListOf())
	{
		this.diaryId.postValue(diaryId)
		this.cropIds.postValue(filterCrops)
	}
}
