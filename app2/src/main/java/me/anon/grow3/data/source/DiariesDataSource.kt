package me.anon.grow3.data.source

import me.anon.grow3.data.model.Diary

interface DiariesDataSource
{
	enum class SyncDirection
	{
		SAVE,
		LOAD
	}

	suspend fun addDiary(diary: Diary): List<Diary>

	suspend fun getDiaryById(diaryId: String): Diary?

	suspend fun getDiaries(): List<Diary>

	suspend fun sync(direction: SyncDirection = SyncDirection.SAVE): List<Diary>
}
