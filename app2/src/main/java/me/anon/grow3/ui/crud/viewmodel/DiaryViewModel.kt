package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.*
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class DiaryViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<DiaryViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryViewModel =
			DiaryViewModel(diariesRepository, handle)
	}

	private var _diaryId: MutableLiveData<String?> = savedStateHandle.getLiveData("diary_id", null)

	private val _diary = _diaryId.switchMap { id ->
		liveData {
			id ?: let {
				val count = diariesRepository.getDiaries().size
				val diary = diariesRepository.createDiary(Diary(name = "Gen ${count + 1}").apply {
					isDraft = true
					crops.add(Crop(
						name = "Crop 1",
						genetics = "Unknown genetics",
						platedDate = this@apply.date
					))
				})

				_diaryId.postValue(diary.id)
				return@liveData
			}

			emitSource(diariesRepository.observeDiary(id!!))
		}
	} as MutableLiveData

	public val diary = _diary.asLiveData()

	public fun setDiaryDate(dateTime: ZonedDateTime)
	{
		_diary.value?.asSuccess()!!.apply {
			date = dateTime.asString()
			_diary.notifyChange()
		}
	}

	public fun setDiaryName(newName: String)
	{
		_diary.value?.apply {
			//name = newName
			_diary.postValue(this)
		}
	}

	public fun addCrop(crop: Crop)
	{
		_diary.value?.asSuccess()!!.apply {
			crops.add(crop)
			_diary.notifyChange()
		}
	}

	public fun refresh()
	{
		_diaryId.postValue(_diaryId.value)
	}
}
