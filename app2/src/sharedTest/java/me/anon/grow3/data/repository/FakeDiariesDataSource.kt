package me.anon.grow3.data.repository

import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.source.DiariesDataSource

/**
 * // TODO: Add class description
 */
public class FakeDiariesDataSource(private val diaries: MutableList<Diary>) : DiariesDataSource
{
	override suspend fun addDiary(diary: Diary): List<Diary> = diaries.apply {
		add(diary)
	}

	override suspend fun getDiaryById(diaryId: String): Diary? = diaries.find { it.id == diaryId }

	override suspend fun getDiaries(): List<Diary> = diaries

	override suspend fun sync(direction: DiariesDataSource.SyncDirection): List<Diary> = diaries
}
