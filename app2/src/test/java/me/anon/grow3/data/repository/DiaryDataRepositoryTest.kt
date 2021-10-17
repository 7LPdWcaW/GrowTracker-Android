package me.anon.grow3.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.TestConstants
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Water
import me.anon.grow3.data.repository.impl.DefaultDiariesRepository
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.initThreeTen
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
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
		val returned = diariesRepository.addDiary(diary)
		diary.`should be equal to`(returned)
		(returned.crops as ArrayList) += Crop(name = "crop 1", genetics = "none", numberOfPlants = 3)
		diary.crops.`should not be empty`()[0].`should be equal to`(returned.crops[0])
//		diariesRepository.sync()
		val diary2 = diariesRepository.getDiaryById(diary.id)
		diary2.`should not be null`()
			.crops.`should not be empty`()[0].`should be equal to`(returned.crops[0])
	}

	@Test
	public fun `test observe draft diary update syncs`() = mainCoroutineRule.runBlockingTest {
		val diary = diariesRepository.addDiary(Diary(name = "test 1").apply { isDraft = true })

		val result = diariesRepository.flowDiary(diary.id).first()
		result.`should not be null`()
			.`should be instance of`(DataResult.Success::class)

		result.asSuccess().let { diary ->
			val crop = Crop(name = "crop 1", genetics = "none", numberOfPlants = 3)
			(diary.crops as ArrayList) += crop
			diary.crops.`should not be empty`()
//			diariesRepository.sync()

			val livedata2 = diariesRepository.flowDiary(diary.id)
			val result2 = livedata2.first()
			result2.`should not be null`()
				.`should be instance of`(DataResult.Success::class)
			result2.asSuccess().crops.`should not be empty`()[0].id.`should be equal to`(crop.id)
		}
	}

	@Test
	public fun `test observe diary sends states`() = mainCoroutineRule.runBlockingTest {
		val diaries = diariesRepository.flowDiaries()
		diaries.first().`should not be null`()
			.`should be instance of`(DataResult.Success::class)
		diaries.first().asSuccess().`should not be empty`()
	}

	@Test
	public fun `test single livedata observer for list`() = mainCoroutineRule.runBlockingTest {
		val diaries1 = diariesRepository.flowDiaries()
			.first()
		val diaries2 = diariesRepository.flowDiaries()
			.first()

		diaries1.`should be equal to`(diaries2)

		// invalidate the data
		diariesRepository.invalidate()

		val diaries3 = diariesRepository.flowDiaries()
			.first()
		diaries3.`should be equal to`(diaries1).`should be equal to`(diaries2)
	}

	@Test
	public fun `test single livedata observer for diary`() = mainCoroutineRule.runBlockingTest {
		val diary1 = (diariesRepository.flowDiary("0000-000000").first() as DataResult.Success).data
		val diary2 = (diariesRepository.flowDiary("0000-000000").first() as DataResult.Success).data

		diary1.`should be equal to`(diary2)

		diary1.name = "changed name"

		// save & invalidate the data
//		diariesRepository.sync()

		val diary3 = (diariesRepository.flowDiary("0000-000000").first() as DataResult.Success).data
		diary3.`should be equal to`(diary1).`should be equal to`(diary2)
		diary2.name.`should be equal to`("changed name")
		diary3.name.`should be equal to`("changed name")
	}

	@Test
	public fun `test invalidate discards changes`() = mainCoroutineRule.runBlockingTest {
		val diaries = diariesRepository.flowDiaries()
		val result = diaries.first()
		val diary = (result as DataResult.Success).data.first()
		val changeName = "changed name"
		diary.name = changeName

		// re-set the repository so [invalidate] loads fresh
		initialiseRepositories()
		diariesRepository.invalidate()

		val diaries2 = diariesRepository.flowDiaries()
		val result2 = diaries2.first()
		val diary2 = (result2 as DataResult.Success).data.first()
		diary2.name.`should not be equal to`(changeName)
	}

	@Test
	public fun `test observe single diary`() = mainCoroutineRule.runBlockingTest {
		val diaryFlow = diariesRepository.flowDiary("0000-000000")
		val result = diaryFlow.first()
		val data = (result as DataResult.Success).data
		data.name = "changed name"
	}

	@Test
	public fun `test cache log`() = mainCoroutineRule.runBlockingTest {
		val diaryFlow = diariesRepository.flowDiary("0000-000000")
		val result = diaryFlow.first()
		val data = (result as DataResult.Success).data
		val log = Water()

		diariesRepository.addLog(log, data)
		val retrieved = diariesRepository.getLog(log.id, data)
		retrieved.`should be equal to`(log)
		data.logOf(log.id).`should be null`()
	}
}
