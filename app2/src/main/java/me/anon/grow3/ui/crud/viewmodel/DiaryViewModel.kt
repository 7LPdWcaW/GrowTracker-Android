package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.clear
import me.anon.grow3.util.states.DataResult

class DiaryViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle,
	private val viewModelScope: CoroutineScope
)
{
	public var isNew: Boolean = false
		get() = savedStateHandle["new_diary"] ?: false
		private set(value) {
			savedStateHandle["new_diary"] = value
			field = value
		}

	private val diaryId: MutableLiveData<String> = savedStateHandle.getLiveData(Extras.EXTRA_DIARY_ID)
	public val diary: LiveData<Diary> = diaryId.switchMap { id ->
		liveData {
			if (id.isNullOrBlank()) return@liveData

			emitSource(diariesRepository.observeDiary(id).switchMap { diaryResult ->
				when (diaryResult)
				{
					is DataResult.Success -> liveData { emit(diaryResult.data) }
					else -> throw GrowTrackerException.DiaryLoadFailed(id)
				}
			})
		}
	}

	public fun new(): LiveData<Diary>
	{
		isNew = true
		diaryId.clear()

		viewModelScope.launch {
			val count = diariesRepository.getDiaries().size
			val diary = Diary(name = "Gen ${count + 1}").apply {
				isDraft = true
				crops as ArrayList += Crop(
					name = "Crop 1",
					genetics = "Unknown genetics",
					platedDate = this@apply.date
				)
			}

			diariesRepository.addDiary(diary)
			diaryId.postValue(diary.id)
		}

		return diary
	}

	public fun load(id: String): LiveData<Diary>
	{
		isNew = false
		diaryId.postValue(id)
		return diary
	}

	public fun remove()
	{
		viewModelScope.launch {
			diaryId.clear()?.let { id ->
				//diariesRepository.removeDiary(id)
			}
		}
	}

	public fun save(new: Diary)
	{
		viewModelScope.launch {
			diariesRepository.addDiary(new)
		}
	}

	public fun clear()
	{
		isNew = false
		diaryId.clear()
		diary.clear()
	}
}
