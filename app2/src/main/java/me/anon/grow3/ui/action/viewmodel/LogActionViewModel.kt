package me.anon.grow3.ui.action.viewmodel

import androidx.lifecycle.*
import com.zhuinden.livedatacombinetuplekt.combineTuple
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException.*
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.Water
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.common.Extras.EXTRA_LOG_TYPE
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.nameOf
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
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

	private val diaryId: String = savedState[EXTRA_DIARY_ID] ?: throw InvalidDiaryId()
	private val logType: String = savedState[EXTRA_LOG_TYPE] ?: throw InvalidLogType()

	public val diary = diariesRepository.observeDiary(diaryId).map { result ->
		when (result)
		{
			is DataResult.Success -> result.asSuccess()
			else -> throw DiaryLoadFailed(diaryId)
		}
	}

	public val logId: LiveData<String?> = MutableLiveData(null)
	public val log: LiveData<Log> = combineTuple(logId, diary).switchMap { (logId, diary) ->
		liveData {
			diary ?: return@liveData
			logId ?: return@liveData

			val log = diariesRepository.getLog(logId, diary) ?: throw LogLoadFailed(logId)
			emit(log)
		}
	}

	public fun editLog(logId: String)
	{

	}

	public fun newLog()
	{
		requireNotNull(diary.value)

		val newLog: Log = when (logType)
		{
			nameOf<Water>() -> Water { }
			nameOf<StageChange>() -> StageChange(diary.value!!.stage().type)
			else -> throw InvalidLogType()
		}

		viewModelScope.launch {
			requireNotNull(diary.value)

			(logId as MutableLiveData).postValue(diariesRepository.addLog(newLog, diary.value!!).id)
		}
	}

	public fun deleteLog()
	{
		viewModelScope.launch {
			requireNotNull(logId.value)
			requireNotNull(diary.value)

			diariesRepository.removeLog(logId.value!!, diary.value!!)
		}
	}

	public fun saveLog()
	{
		viewModelScope.launch {
			requireNotNull(log.value)
			requireNotNull(diary.value)

			diariesRepository.addLog(log.value!!, diary.value!!)
		}
	}

	public fun clear()
	{
		(logId as MutableLiveData).postValue(null)
	}
}
