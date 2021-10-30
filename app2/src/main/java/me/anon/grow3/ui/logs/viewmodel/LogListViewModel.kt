package me.anon.grow3.ui.logs.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras.EXTRA_CROP_IDS
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class LogListViewModel constructor(
	diariesRepository: DiariesRepository,
	savedState: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<LogListViewModel>
	{
		override fun create(handle: SavedStateHandle): LogListViewModel =
			LogListViewModel(diariesRepository, handle)
	}

	sealed class UiResult
	{
		data class Loaded(val diary: Diary, val logs: List<Log>, val crops: List<Crop>? = null, val nonce: Long = System.currentTimeMillis()) : UiResult()
		object Loading : UiResult()
		object Error : UiResult()
	}

	public var diaryId: String = savedState[EXTRA_DIARY_ID] ?: ""; private set
	public var cropIds: List<String> = (savedState.get(EXTRA_CROP_IDS) as? Array<String>)?.asList() ?: arrayListOf(); private set

	private var _diary: Flow<Diary> = diariesRepository.flowDiary(diaryId)
		.mapLatest {
			when (it)
			{
				is DataResult.Success -> it.data
				else -> throw GrowTrackerException.DiaryLoadFailed()
			}
		}

	private var _state = MutableStateFlow<UiResult>(UiResult.Loading)
	public val state: StateFlow<UiResult> = _state

	init {
		viewModelScope.launch {
			_diary
				.catch { error ->
					_state.emit(UiResult.Error)
				}
				.collectLatest { diary ->
					val logs = diary.log
						.filter {
							if (cropIds.isNullOrEmpty()) true
							else it.cropIds.containsAll(cropIds)
						}
						.filterNot { it.isDraft }
					val crops = cropIds.map { diary.crop(it) }

					_state.emit(UiResult.Loaded(diary = diary, logs = logs, crops = crops))
				}
		}
	}
}
