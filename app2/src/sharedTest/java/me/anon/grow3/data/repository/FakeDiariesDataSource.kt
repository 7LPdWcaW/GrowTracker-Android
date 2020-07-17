package me.anon.grow3.data.repository

import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.parseAsDiaries
import me.anon.grow3.util.toJsonString

public class FakeDiariesDataSource(private val diaries: MutableList<Diary>) : DiariesDataSource
{
	private var cachedData = diaries.toJsonString()
	private var tempCache = arrayListOf<Any>()

	override suspend fun addDiary(diary: Diary): List<Diary> = diaries.apply {
		add(diary)
		cachedData = diaries.toJsonString()
	}

	override suspend fun deleteDiary(diaryId: String): List<Diary>
	{
		diaries.removeAll { it.id == diaryId }
		return diaries
	}

	override fun close()
	{
		diaries.clear()
	}

	override suspend fun getDiaryById(diaryId: String): Diary? = diaries.find { it.id == diaryId }

	override suspend fun getDiaries(): List<Diary> = diaries

	override suspend fun sync(direction: DiariesDataSource.SyncDirection, vararg diary: Diary): List<Diary>
	{
		when (direction)
		{
			DiariesDataSource.SyncDirection.LOAD -> {
				diaries.clear()
				diaries.addAll(cachedData.parseAsDiaries())
			}

			DiariesDataSource.SyncDirection.SAVE ->
			{
				cachedData = diaries.toJsonString()
			}

		}

		return diaries
	}
}
