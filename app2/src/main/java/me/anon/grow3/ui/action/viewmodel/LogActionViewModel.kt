package me.anon.grow3.ui.action.viewmodel

import androidx.lifecycle.*
import com.zhuinden.livedatacombinetuplekt.combineTuple
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.*
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.Water
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.Extras.EXTRA_LOG_TYPE
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.clear
import me.anon.grow3.util.nameOf
import me.anon.grow3.util.states.Data
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class LogActionViewModel constructor(
	private val diariesRepository: DiariesRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<LogActionViewModel>
	{
		override fun create(handle: SavedStateHandle): LogActionViewModel =
			LogActionViewModel(diariesRepository, handle)
	}

	public var isNew: Boolean = false
		get() = savedState["new_log"] ?: false
		private set(value) {
			savedState["new_log"] = value
			field = value
		}

	private val diaryId: MutableLiveData<String> = savedState.getLiveData(Extras.EXTRA_DIARY_ID)
	private val logId: MutableLiveData<String> = savedState.getLiveData(Extras.EXTRA_LOG_ID)
	private val logType: String = savedState[EXTRA_LOG_TYPE] ?: throw InvalidLogType()

	public val log: LiveData<Data> = combineTuple(diaryId, logId).switchMap { (diaryId, logId) ->
		liveData {
			if (logId.isNullOrBlank() || diaryId.isNullOrBlank()) return@liveData

			// should this react to changes on the diary?
			emitSource(diariesRepository.observeDiary(diaryId).switchMap { diaryResult ->
				when (diaryResult)
				{
					is DataResult.Success -> liveData {
						val diary = diaryResult.data
						val log = diariesRepository.getLog(logId, diary) ?: throw LogLoadFailed(logId)
						emit(Data(diary = diary, log = log))
					}
					else -> throw LogLoadFailed(logId)
				}
			})
		}
	}

	public fun load(id: String): LiveData<Data>
	{
		isNew = false
		logId.postValue(id)
		return log
	}

	public fun new(): LiveData<Data>
	{
		isNew = true
		logId.clear()

		viewModelScope.launch {
			diaryId.value?.let { diaryId ->
				val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed(diaryId)

				val newLog: Log = when (logType)
				{
					nameOf<Water>() -> Water { }
					nameOf<StageChange>() -> StageChange(diary.stage().type)
					else -> throw InvalidLogType()
				}

				newLog.isDraft = true

				diariesRepository.addLog(newLog, diary)
				logId.postValue(newLog.id)
			}
		}

		return log
	}

	public fun remove()
	{
		viewModelScope.launch {
			val diary = log.value?.diary ?: return@launch
			val log = log.value?.log ?: return@launch
			diariesRepository.removeLog(log.id, diary)
		}
	}

	public fun save(new: Log)
	{
		isNew = false
		viewModelScope.launch {
			val diaryId = diaryId.value ?: return@launch
			val diary = diariesRepository.getDiaryById(diaryId) ?: throw DiaryLoadFailed(diaryId)
			new.isDraft = false
			diariesRepository.addLog(new, diary)
		}
	}

	public fun clear()
	{
		if (isNew || log.value?.log?.isDraft == true)
		{
			remove()
		}

		logId.clear()
		log.clear()
		isNew = false
	}
}
