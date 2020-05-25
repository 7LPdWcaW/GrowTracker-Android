package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ViewModelFactory
import me.anon.grow3.util.asString
import me.anon.grow3.util.toLiveData
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

	private var _diaryId: String? = savedStateHandle["diary_id"]
		set(value) {
			savedStateHandle["diary_id"] = value
			field = value
		}

	private val _diary = liveData {
		val count = diariesRepository.getDiaries().size
		val diary = _diaryId?.let { diariesRepository.getDiaryById(it) }
			?: diariesRepository.createDiary(Diary(name = "Gen ${count + 1}").apply {
				crops.add(Crop(
					name = "Crop 1",
					genetics = "Unknown genetics",
					platedDate = this@apply.date
				))
			}, true)

		_diaryId = diary.id
		emit(diary)

	} as MutableLiveData

	public val diary = _diary.toLiveData()

	public fun setDiaryDate(dateTime: ZonedDateTime)
	{
		_diary.value?.apply {
			date = dateTime.asString()
			_diary.postValue(this)
		}
	}

	public fun setDiaryName(newName: String)
	{
		_diary.value?.apply {
			name = newName
			_diary.postValue(this)
		}
	}

	public fun addCrop(crop: Crop)
	{
		_diary.value?.apply {
			crops.add(crop)
			_diary.postValue(this)
		}
	}
}
