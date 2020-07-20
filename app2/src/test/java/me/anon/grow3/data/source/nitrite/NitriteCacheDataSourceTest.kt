package me.anon.grow3.data.source.nitrite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Water
import me.anon.grow3.util.initThreeTen
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class NitriteCacheDataSourceTest
{
	// Set the main coroutines dispatcher for unit testing.
	@ExperimentalCoroutinesApi
	@get:Rule
	public val mainCoroutineRule = MainCoroutineRule()

	// Executes each task synchronously using Architecture Components.
	@get:Rule
	public val instantExecutorRule = InstantTaskExecutorRule()

	private lateinit var dataSource: NitriteCacheDataSource

	private val testDispatcher = TestCoroutineDispatcher()

	init {
		initThreeTen()
	}

	@Before
	public fun initialiseDataSource()
	{
		Dispatchers.setMain(testDispatcher)

		val file = File.createTempFile("cache-${System.currentTimeMillis()}", ".db").apply {
			deleteOnExit()
		}

		dataSource = NitriteCacheDataSource(file.absolutePath)
	}

	@After
	public fun cleanup()
	{
		Dispatchers.resetMain()
		testDispatcher.cleanupTestCoroutines()
	}

	@Test
	public fun `test cache log`() = runBlocking<Unit> {
		val log = Water {
			inPH = Water.PHUnit(1.0)
			outPH = Water.PHUnit(2.0)
		}
		val id = dataSource.cache(log)

		val retrieved = dataSource.retrieveLog(id) as Water
		retrieved.`should be equal to`(log)
	}

	@Test
	public fun `test cache crop`() = runBlocking<Unit> {
		val crop = Crop(name = "crop 1")
		val id = dataSource.cache(crop)

		val retrieved = dataSource.retrieveCrop(id)
		retrieved.`should be equal to`(crop)
	}

	@Test
	public fun `test cache diary`() = runBlocking<Unit> {
		val diary = Diary(name = "diary 1")
		val id = dataSource.cache(diary)

		val retrieved = dataSource.retrieveDiary(id)
		retrieved.`should be equal to`(diary)
	}

	@Test
	public fun `test cache map`() = runBlocking<Unit> {
		val map = mapOf(
			"key1" to "value1",
			"key2" to 2,
			"key3" to true,
			"key4" to null
		)
		val id = dataSource.cache(map)

		val retrieved = dataSource.retrieveMap(id)
		retrieved.`should contain all`(map)
	}
}
