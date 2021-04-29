package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.ValueHolder
import me.anon.grow3.util.ViewModelFactory
import javax.inject.Inject

class DiaryCrudViewModel(
	private val diariesRepository: DiariesRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel()
{
	class Factory @Inject constructor(
		private val diariesRepository: DiariesRepository
	) : ViewModelFactory<DiaryCrudViewModel>
	{
		override fun create(handle: SavedStateHandle): DiaryCrudViewModel =
			DiaryCrudViewModel(diariesRepository, handle)
	}

	sealed class UiResult
	{
		data class Loaded(val diary: Diary, val crop: Crop? = null) : UiResult()
		object Loading : UiResult()
	}

	private var diary: Flow<Diary> = flowOf()
	private var crop: Flow<Crop> = flowOf()
	public val state = MutableStateFlow<UiResult>(UiResult.Loading)

	private val diaryVm = DiaryUseCase(diariesRepository)
	private val cropVm = CropUseCase(diariesRepository)

	public fun mutateDiary(block: Diary.() -> Diary)
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			diaryVm.save(block(diary))
		}
	}

	public fun mutateCrop(block: Crop.() -> Crop)
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			val crop = (state.value as? UiResult.Loaded)?.crop ?: return@launch
			cropVm.save(diary, block(crop))
		}
	}

	public fun endCrop()
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			state.emit(UiResult.Loaded(diary))
		}
	}

	public fun setCropMedium(mediumType: ValueHolder<MediumType>? = null, volume: ValueHolder<Volume?>? = null)
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			val crop = (state.value as? UiResult.Loaded)?.crop ?: return@launch

			// medium - only 1 medium type to set
			val medium = diary.mediumOf(crop)
				?: let {
					mediumType?.let { type ->
						Medium(type.value).also {
							diariesRepository.addLog(it, diary)
						}
					}
				}

			medium?.apply {
				mediumType?.applyValue { this.medium = it }
				volume?.applyValue { this.size = it }
				diariesRepository.addLog(this, diary)
			}
		}
	}

	public fun newDiary()
	{
		viewModelScope.launch {
			diary = diaryVm.new()
			diary.collect {
				state.emit(UiResult.Loaded(it))
			}
		}
	}

	public fun newCrop()
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			crop = cropVm.new(diary)
			crop.collect {
				state.emit(UiResult.Loaded(diary, it))
			}
		}
	}

	public fun loadCrop(id: String)
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			crop = cropVm.load(diary, id)
			crop.collect {
				state.emit(UiResult.Loaded(diary, it))
			}
		}
	}

	public fun removeCrop()
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			cropVm.remove(diary)
			crop.collect {
				state.emit(UiResult.Loaded(diary))
			}
		}
	}

	public fun complete()
	{
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			diary.isDraft = false
			diaryVm.save(diary)
			state.emit(UiResult.Loaded(diary))
		}
	}
}
