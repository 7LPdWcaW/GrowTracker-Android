package me.anon.grow3.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.TestConstants
import me.anon.grow3.data.model.Crop
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
	public fun `test draft diary update syncs`() = mainCoroutineRule.runBlockingTest {
		val diary = Diary(name = "test 1").apply { isDraft = true }
		val returned = diariesRepository.createDiary(diary)
		diary.`should be equal to`(returned)
		returned.crops += Crop(name = "crop 1", genetics = "none", numberOfPlants = 3)
		diary.crops.`should not be empty`()[0].`should be equal to`(returned.crops[0])
		diariesRepository.sync()
		val diary2 = diariesRepository.getDiaryById(diary.id)
		diary2.`should not be null`()
			.crops.`should not be empty`()[0].`should be equal to`(returned.crops[0])
	}

	@Test
	public fun `test observe draft diary update syncs`() = mainCoroutineRule.runBlockingTest {
		val diary = diariesRepository.createDiary(Diary(name = "test 1").apply { isDraft = true })

		var livedata = diariesRepository.observeDiary(diary.id)
 		val result = livedata.getOrAwaitValue()
		result.`should not be null`()
			.`should be instance of`(DataResult.Success::class)

		result.asSuccess().let { diary ->
			val crop = Crop(name = "crop 1", genetics = "none", numberOfPlants = 3)
			diary.crops += crop
			diary.crops.`should not be empty`()
			diariesRepository.sync()

			var livedata2 = diariesRepository.observeDiary(diary.id)
			val result2 = livedata2.getOrAwaitValue()
			result2.`should not be null`()
				.`should be instance of`(DataResult.Success::class)
			result2.asSuccess().crops.`should not be empty`()[0].id.`should be equal to`(crop.id)
		}
	}

	@Test
	public fun `test observe diary sends states`()
	{
		val diaries = diariesRepository.observeDiaries()
		diaries.getOrAwaitValue().`should not be null`()
			.`should be instance of`(DataResult.Success::class)
		diaries.getOrAwaitValue().asSuccess().`should not be empty`()
	}

	@Test
	public fun `test single livedata observer for list`() = mainCoroutineRule.runBlockingTest {
		val diaries1 = diariesRepository.observeDiaries()
			.awaitForSuccess()
		val diaries2 = diariesRepository.observeDiaries()
			.awaitForSuccess()

		diaries1.`should be equal to`(diaries2)

		// invalidate the data
		diariesRepository.invalidate()

		val diaries3 = diariesRepository.observeDiaries()
			.awaitForSuccess()
		diaries3.`should be equal to`(diaries1).`should be equal to`(diaries2)
	}

	@Test
	public fun `test single livedata observer for diary`() = mainCoroutineRule.runBlockingTest {
		System.out.println("diary1")
		val diary1 = diariesRepository.observeDiary("0000-000000")
			.awaitForSuccess()
		System.out.println("diary2")
		val diary2 = diariesRepository.observeDiary("0000-000000")
			.awaitForSuccess()

		diary1.`should be equal to`(diary2)

		diary1.name = "changed name"

		// save & invalidate the data
		diariesRepository.sync()

		val diary3 = diariesRepository.observeDiary("0000-000000")
			.awaitForSuccess()
		diary3.`should be equal to`(diary1).`should be equal to`(diary2)
		diary2.name.`should be equal to`("changed name")
		diary3.name.`should be equal to`("changed name")
	}

	@Test
	public fun `test invalidate discards changes`() = mainCoroutineRule.runBlockingTest {
		val diaries = diariesRepository.observeDiaries()
			.awaitForSuccess()
		val diary = diaries.first()
		val changeName = "changed name"
		diary.name = changeName

		// re-set the repository so [invalidate] loads fresh
		initialiseRepositories()
		diariesRepository.invalidate()

		val diaries2 = diariesRepository.observeDiaries()
			.awaitForSuccess()
		val diary2 = diaries2.first()
		diary2.name.`should not be equal to`(changeName)
	}

	@Test
	public fun `test observe single diary`() = mainCoroutineRule.runBlockingTest {
		val diary = diariesRepository.observeDiary("0000-000000")
		val data = diary.awaitForSuccess()
		data.name = "changed name"
	}
}
