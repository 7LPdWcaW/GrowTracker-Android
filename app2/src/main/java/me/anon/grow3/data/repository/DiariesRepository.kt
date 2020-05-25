package me.anon.grow3.data.repository

import androidx.lifecycle.LiveData
import me.anon.grow3.data.model.Diary
import me.anon.grow3.util.DataResult

interface DiariesRepository
{
	public fun observeDiaries(): LiveData<DataResult<List<Diary>>>

	public fun observeDiary(gardenId: String): LiveData<DataResult<Diary>>

	public suspend fun getDiaries(): List<Diary>

	public suspend fun getDiaryById(diaryId: String): Diary?

	public suspend fun createDiary(diary: Diary, isDraft: Boolean = false): Diary

	public fun sync()

	public fun invalidate()
}
