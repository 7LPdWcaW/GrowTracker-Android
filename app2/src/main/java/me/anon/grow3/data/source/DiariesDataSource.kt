package me.anon.grow3.data.source

import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log

interface DiariesDataSource
{
	enum class SyncDirection
	{
		SAVE,
		LOAD
	}

	public fun close()

	suspend fun addDiary(diary: Diary): List<Diary>
	suspend fun deleteDiary(diaryId: String): List<Diary>

	suspend fun cache(log: Log): Log
	suspend fun get(logId: String): Log?

	suspend fun getDiaryById(diaryId: String): Diary?

	suspend fun getDiaries(): List<Diary>

	suspend fun sync(direction: SyncDirection = SyncDirection.SAVE, vararg diary: Diary): List<Diary>
}
