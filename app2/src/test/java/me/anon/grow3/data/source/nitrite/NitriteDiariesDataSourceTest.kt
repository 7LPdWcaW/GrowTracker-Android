package me.anon.grow3.data.source.nitrite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.TestConstants
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.util.initThreeTen
import org.amshove.kluent.*
import org.dizitart.no2.exceptions.UniqueConstraintException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class NitriteDiariesDataSourceTest
{
	// Set the main coroutines dispatcher for unit testing.
	@ExperimentalCoroutinesApi
	@get:Rule
	public val mainCoroutineRule = MainCoroutineRule()

	// Executes each task synchronously using Architecture Components.
	@get:Rule
	public val instantExecutorRule = InstantTaskExecutorRule()

	private lateinit var dataSource: NitriteDiariesDataSource

	private val testDispatcher = TestCoroutineDispatcher()

	init {
		initThreeTen()
	}

	@Before
	public fun initialiseDataSource()
	{
		Dispatchers.setMain(testDispatcher)

		val file = File.createTempFile("diaries-${System.currentTimeMillis()}", ".db").apply {
			deleteOnExit()
		}

		dataSource = NitriteDiariesDataSource(file.absolutePath)

		runBlocking {
			dataSource.getDiaries().`should be empty`()

//			// populate temp file
//			file.writeBytes(TestConstants.diaries_json.toByteArray())
//
//			dataSource.sync(DiariesDataSource.SyncDirection.LOAD)
//			dataSource.getDiaries().`should not be empty`()
		}
	}

	@After
	public fun cleanup()
	{
		Dispatchers.resetMain()
		testDispatcher.cleanupTestCoroutines()
	}

	@Test
	public fun `test add diary and sync`() = runBlocking<Unit> {
		dataSource.getDiaries().`should not contain`(TestConstants.newDiary)
		dataSource.addDiary(TestConstants.newDiary)
		dataSource.getDiaries().`should contain`(TestConstants.newDiary)
	}

	@Test
	public fun `test add duplicate diary`() = runBlocking<Unit> {
		dataSource.addDiary(TestConstants.newDiary)

		try
		{
			dataSource.addDiary(TestConstants.newDiary)
			throw Exception()
		}
		catch (e: Exception)
		{
			e.`should be instance of`(UniqueConstraintException::class)
		}
	}

	@Test
	public fun `test edit diary`() = runBlocking<Unit> {
		val diaries1 = dataSource.getDiaries()
		val diaries2 = dataSource.addDiary(TestConstants.newDiary)

		// database is not auto updated/saved when add
		diaries1.`should not be equal to`(diaries2)
		diaries2.`should not be empty`()

		val diary = dataSource.getDiaryById(TestConstants.newDiary.id)
		diary
			.`should not be null`()

		val updateString = "Updated name"
		diary.name = updateString

		// transactional databases require re-saving
		dataSource.sync(DiariesDataSource.SyncDirection.Commit, diary)

		// diary in backing list should be updated as [diary] should be an object reference
		with (dataSource.getDiaryById(diary.id)) {
			this.`should not be null`()
			name.`should be`(updateString)
		}
	}

	@Test
	public fun `test load from disk`() = runBlocking<Unit> {
		dataSource.addDiary(TestConstants.newDiary)
		dataSource.sync(DiariesDataSource.SyncDirection.Commit)
		val diary = dataSource.getDiaryById(TestConstants.newDiary.id)
			.`should not be null`()

		val updateString = "Updated name"
		diary.name = updateString

		dataSource.sync(DiariesDataSource.SyncDirection.Commit)

		// we didn't save so the diary should not be updated
		val diary2 = dataSource.getDiaryById(TestConstants.newDiary.id)
		diary2.`should not be null`()
		diary2.hashCode().`should not be equal to`(diary.hashCode())
		diary2.name.`should not be equal to`(updateString)
	}
}
