package me.anon.grow3.data.repository

import androidx.lifecycle.LiveData
import com.zhuinden.eventemitter.EventSource
import me.anon.grow3.data.event.LogEvent
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.util.states.DataResult

interface DiariesRepository
{
	public fun observeDiaries(): LiveData<DataResult<List<Diary>>>

	public fun observeDiary(diaryId: String): LiveData<DataResult<Diary>>

	public fun observeLogEvents(): EventSource<LogEvent>

	public suspend fun getDiaries(): List<Diary>

	public suspend fun getDiaryById(diaryId: String): Diary?

	public suspend fun addDiary(diary: Diary): Diary
	public suspend fun deleteDiary(diaryId: String): Boolean

	public suspend fun addLog(log: Log, diary: Diary): Log
	public suspend fun getLog(logId: String, diary: Diary): Log?
	public suspend fun removeLog(logId: String, diary: Diary)

	public suspend fun addCrop(crop: Crop, diary: Diary): Crop
	public suspend fun getCrop(cropId: String, diary: Diary): Crop?
	public suspend fun removeCrop(cropId: String, diary: Diary)

	public fun sync()

	public fun invalidate()
}
