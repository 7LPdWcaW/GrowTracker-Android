package me.anon.grow3.ui.crud.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.StageType
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.util.states.DataResult

class CropUseCase(
	private val diariesRepository: DiariesRepository
)
{
	private var originalCrop: Crop? = null
	private var crop: Crop? = null

	public suspend fun new(diary: Diary): Flow<Crop>
	{
		val count = diary.crops.size + 1
		val crop = Crop(
			name = "Crop $count",
			genetics = "Unknown genetics"
		)
		crop.isDraft = true

		diariesRepository.addCrop(crop, diary)

		if (diary.stageOf(crop) == null)
		{
			// add default stage
			diariesRepository.addLog(StageChange(StageType.Planted).apply {
				date = crop.platedDate
				cropIds += crop.id
			}, diary)
		}

		return diariesRepository.flowDiary(diary.id)
			.map { result ->
				when (result)
				{
					is DataResult.Success -> {
						originalCrop = crop.copy()
						result.data.crop(crop.id)
					}
					else -> throw GrowTrackerException.CropLoadFailed(crop.id)
				}
			}
	}

	public fun load(diary: Diary, cropId: String): Flow<Crop>
	{
		return diariesRepository.flowDiary(diary.id)
			.map { result ->
				when (result)
				{
					is DataResult.Success ->
					{
						val crop = result.data.crop(cropId)
						crop.isDraft = false
						originalCrop = crop.copy()
						result.data.crop(crop.id)
					}
					else -> throw GrowTrackerException.DiaryLoadFailed(diary.id)
				}
			}
	}

	public suspend fun remove(diary: Diary)
	{
		val crop = crop ?: return
		diariesRepository.removeCrop(crop.id, diary)
		this.crop = null
		this.originalCrop = null
	}

	public suspend fun save(diary: Diary, crop: Crop)
	{
		this.crop = diariesRepository.addCrop(crop, diary)
	}

	public fun clear()
	{
		this.crop = null
		this.originalCrop = null
//		isNew = false
//		cropId = ""
	}
}
