package me.anon.grow3.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.TestConstants
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.repository.impl.DefaultDiariesRepository
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.*
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DiaryDataRepositoryTest
{
	// Set the main coroutines dispatcher for unit testing.
	@ExperimentalCoroutinesApi
	@get:Rule
	public val mainCoroutineRule = MainCoroutineRule()

	// Executes each task synchronously using Architecture Components.
	@get:Rule
	public val instantExecutorRule = InstantTaskExecutorRule()

	private lateinit var dataSource: DiariesDataSource
	private lateinit var diariesRepository: DefaultDiariesRepository

	init {
		initThreeTen()
	}

	@Before
	public fun initialiseRepositories()
	{
		dataSource = FakeDiariesDataSource(TestConstants.diaries.toMutableList())
		diariesRepository = DefaultDiariesRepository(dataSource)
	}

	@Test
	public fun `test flow`() = mainCoroutineRule.runBlockingTest {
		val diaries = diariesRepository.testGetDiaries.first()
		diaries.`should not be null`()
		diaries.`should not be empty`()

		val test2 = diariesRepository.testGetDiaryById("0000-000000").first()
		test2.`should not be null`()
	}

	@Test
	public fun `test observe diary sends states`()
	{
		val diaries = diariesRepository.observeDiaries()
		diaries.getOrAwaitValue().`should be equal to`(DataResult.Loading)
		diaries.getOrAwaitValue().`should not be null`()
			.`should be instance of`(DataResult.Success::class)
		diaries.getOrAwaitValue().asSuccess().`should not be empty`()
	}

	@Test
	public fun `test single livedata observer`() = mainCoroutineRule.runBlockingTest {
		val diaries1 = diariesRepository.observeDiaries()
		diaries1.awaitResult().`should be instance of`(DataResult.Success::class)
		val diaries2 = diariesRepository.observeDiaries()
		diaries2.awaitResult().`should be instance of`(DataResult.Success::class)

		diaries1.`should be equal to`(diaries2)

		// invalidate the data
		diariesRepository.invalidate()

		val diaries3 = diariesRepository.observeDiaries()
		diaries3.`should be equal to`(diaries1).`should be equal to`(diaries2)
	}

	@Test
	public fun `test invalidate discards changes`() = mainCoroutineRule.runBlockingTest {
		val diaries = diariesRepository.observeDiaries().awaitResult()
		val diary = diaries.asSuccess().first()
		val changeName = "changed name"
		diary.name = changeName

		// re-set the repository so [invalidate] loads fresh
		initialiseRepositories()
		diariesRepository.invalidate()

		val diaries2 = diariesRepository.observeDiaries().awaitResult()
		val diary2 = diaries2.asSuccess().first()
		diary2.name.`should not be equal to`(changeName)
	}

	@Test
	public fun `test observe single diary`() = mainCoroutineRule.runBlockingTest {
		val diary = diariesRepository.observeDiary("0000-000000")
		val data = diary.awaitResult().asSuccess()
		data.name = "changed name"
	}
}
