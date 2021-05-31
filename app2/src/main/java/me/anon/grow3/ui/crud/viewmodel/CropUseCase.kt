package me.anon.grow3.ui.crud.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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
		val temp = Crop(
			name = "Crop $count",
			genetics = "Unknown genetics"
		)
		temp.isDraft = true

		diariesRepository.addCrop(temp, diary)

		if (diary.stageOf(temp) == null)
		{
			// add default stage
			diariesRepository.addLog(StageChange(StageType.Planted).apply {
				date = temp.platedDate
				cropIds += temp.id
			}, diary)
		}

		return diariesRepository.flowDiary(diary.id)
			.mapLatest { result ->
				when (result)
				{
					is DataResult.Success -> {
						originalCrop = temp.copy()
						crop = diariesRepository.getCrop(temp.id, result.data) ?: throw GrowTrackerException.CropLoadFailed(temp.id)
						crop!!
					}
					else -> throw GrowTrackerException.DiaryLoadFailed(diary.id)
				}
			}
	}

	public fun load(diary: Diary, cropId: String): Flow<Crop>
	{
		return diariesRepository.flowDiary(diary.id)
			.map { result ->
				when (result)
				{
					is DataResult.Success -> {
						crop = diariesRepository.getCrop(cropId, result.data) ?: throw GrowTrackerException.CropLoadFailed(cropId, result.data.id)
						crop!!.isDraft = false
						originalCrop = crop!!.copy()
						crop!!
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
	}
}
