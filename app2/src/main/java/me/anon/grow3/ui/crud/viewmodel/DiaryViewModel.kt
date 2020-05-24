package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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

	private val _diary = MutableLiveData(Diary(name = ""))
	public val diary = _diary.toLiveData()

	public fun setDiaryDate(dateTime: ZonedDateTime)
	{
		_diary.value?.apply {
			date = dateTime.asString()
			_diary.postValue(this)
		}
	}
}
