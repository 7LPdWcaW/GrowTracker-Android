package me.anon.grow3.ui.crud.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.states.DataResult

class DiaryUseCase(
	private val diariesRepository: DiariesRepository
)
{
	private var diary: Diary? = null
	private var isNew: Boolean = false

	public suspend fun new(): Flow<Diary> = flow {
		isNew = true
		val count = diariesRepository.getDiaryCount(false)
		val newDiary = Diary(name = "Gen ${count + 1}").apply {
			isDraft = true
//			crops as ArrayList += Crop(
//				name = "Crop 1",
//				genetics = "Unknown genetics",
//				platedDate = this@apply.date
//			)
		}

		diary = diariesRepository.addDiary(newDiary)
		emitAll(diariesRepository.flowDiary(newDiary.id)
			.map { result ->
				when (result)
				{
					is DataResult.Success -> {
						diary = result.data
						result.data
					}
					else -> throw GrowTrackerException.DiaryLoadFailed(newDiary.id)
				}
			})
	}

	public fun load(id: String): Flow<Diary>
		= diariesRepository.flowDiary(id)
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

	public fun latest(): Diary = diary!!

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