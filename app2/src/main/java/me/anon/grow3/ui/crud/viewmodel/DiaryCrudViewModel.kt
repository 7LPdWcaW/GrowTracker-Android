package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
	private val diaryScope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())
	private val cropScope = CoroutineScope(diaryScope.coroutineContext + SupervisorJob())

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

	private val _state = MutableStateFlow<UiResult>(UiResult.Loading)
	public val state: StateFlow<UiResult> = _state

	private val diaryVm = DiaryUseCase(diariesRepository)
	private val cropVm = CropUseCase(diariesRepository)

	public val diaryDraft: Boolean get() = (state.value as? UiResult.Loaded)?.diary?.isDraft ?: false

	public fun mutateDiary(block: Diary.() -> Diary)
	{
		diaryScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			diaryVm.save(block(diary))
		}
	}

	public fun mutateCrop(block: Crop.() -> Crop)
	{
		cropScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			val crop = (state.value as? UiResult.Loaded)?.crop ?: return@launch
			cropVm.save(diary, block(crop))
		}
	}

	public fun mutateEnvironment(block: Environment.() -> Environment)
	{
		diaryScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			val environment: Environment = diary.environment()
				?: Environment().apply {
					diariesRepository.addLog(this, diary)
				}

			block(environment)
			diariesRepository.addLog(environment, diary)
		}
	}

	public fun endCrop()
	{
		cropScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			_state.emit(UiResult.Loaded(diary))
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
				mediumType?.patch { this.medium = it }
				volume?.patch { this.size = it }
				diariesRepository.addLog(this, diary)
			}
		}
	}

	public fun newDiary()
	{
		diaryScope.launch {
			val diary = diaryVm.new()
			diary.collect {
				_state.emit(UiResult.Loaded(it))
			}
		}
	}

	public fun loadDiary(id: String)
	{
		diaryScope.launch {
			val diary = diaryVm.load(id)
			diary.collect {
				_state.emit(UiResult.Loaded(it))
			}
		}
	}

	public fun newCrop()
	{
		cropScope.coroutineContext.cancelChildren()
		cropScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			val crop = cropVm.new(diary)
			crop.collect {
				_state.emit(UiResult.Loaded(diary, it))
			}
		}
	}

	public fun loadCrop(id: String)
	{
		cropScope.coroutineContext.cancelChildren()
		cropScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			val crop = cropVm.load(diary, id)
			crop.collect {
				_state.emit(UiResult.Loaded(diary, it))
			}
		}
	}

	public fun removeCrop()
	{
		cropScope.coroutineContext.cancelChildren()
		cropScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			cropVm.remove(diary)
		}
	}

	public fun complete()
	{
		cropScope.coroutineContext.cancelChildren()
		diaryScope.coroutineContext.cancelChildren()
		viewModelScope.launch {
			val diary = (state.value as? UiResult.Loaded)?.diary ?: return@launch
			diary.isDraft = false
			diaryVm.save(diary)
			_state.emit(UiResult.Loaded(diary))
		}
	}

	public fun cancel()
	{
		cropScope.coroutineContext.cancelChildren()
		diaryScope.coroutineContext.cancelChildren()
		viewModelScope.launch {
			_state.emit(UiResult.Loading)
			diaryVm.remove()
		}
	}
}
