package me.anon.grow3.ui.crud.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.*
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.states.DataResult

class DiaryUseCase(
	private val diariesRepository: DiariesRepository
)
{
	private var diary: Diary? = null
	public var isNew: Boolean = false; private set

	public fun cached(): Diary = diary ?: throw GrowTrackerException.IllegalState("Diary was null")

	public suspend fun new(): Flow<Diary>
	{
		isNew = true
		val count = diariesRepository.getDiaries().filter { !it.isDraft }.size
		val newDiary = Diary(name = "Gen ${count + 1}").apply {
			isDraft = true
			crops as ArrayList += Crop(
				name = "Crop 1",
				genetics = "Unknown genetics",
				platedDate = this@apply.date
			)
		}

		diary = diariesRepository.addDiary(newDiary)
		return diariesRepository.flowDiary(newDiary.id)
			.map { result ->
				when (result)
				{
					is DataResult.Success -> result.data
					else -> throw GrowTrackerException.DiaryLoadFailed(newDiary.id)
				}
			}
	}

	public fun load(id: String): Flow<Diary>
	{
		return diariesRepository.flowDiary(id)
			.map { result ->
				when (result)
				{
					is DataResult.Success -> {
						diary = result.data
						result.data
					}
					else -> throw GrowTrackerException.DiaryLoadFailed(id)
				}
			}
	}

	public suspend fun remove()
	{
		val id = this.diary?.id ?: return
		diariesRepository.deleteDiary(id)
	}

	public suspend fun save(new: Diary)
	{
		this.diary = diariesRepository.addDiary(new)
	}

	public fun clear()
	{
//		isNew = false
//		diaryId = ""
		//state.clear()
	}
}
