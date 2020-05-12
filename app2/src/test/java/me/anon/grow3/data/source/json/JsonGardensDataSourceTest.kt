package me.anon.grow3.data.source.json

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.TestConstants
import me.anon.grow3.data.source.GardensDataSource
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class JsonGardensDataSourceTest
{
	// Set the main coroutines dispatcher for unit testing.
	@ExperimentalCoroutinesApi
	@get:Rule
	public val mainCoroutineRule = MainCoroutineRule()

	// Executes each task synchronously using Architecture Components.
	@get:Rule
	public val instantExecutorRule = InstantTaskExecutorRule()

	private lateinit var dataSource: JsonGardensDataSource

	private val testDispatcher = TestCoroutineDispatcher()

	@Before
	public fun initialiseDataSource()
	{
		Dispatchers.setMain(testDispatcher)

		val file = File.createTempFile("gardens-${System.currentTimeMillis()}", ".json").apply {
			deleteOnExit()
		}
		dataSource = JsonGardensDataSource(file.absolutePath)

		runBlocking {
			dataSource.getGardens().`should be empty`()

			// populate temp file
			file.writeBytes(TestConstants.gardens_json.toByteArray())

			dataSource.sync(GardensDataSource.SyncDirection.LOAD)
			dataSource.getGardens().`should not be empty`()
		}
	}

	@After
	public fun cleanup()
	{
		Dispatchers.resetMain()
		testDispatcher.cleanupTestCoroutines()
	}

	@Test
	public fun `test add garden and sync`() = runBlocking<Unit> {
		dataSource.getGardens().`should not contain`(TestConstants.newGarden)
		dataSource.addGarden(TestConstants.newGarden)
		dataSource.getGardens().`should contain`(TestConstants.newGarden)
	}

	@Test
	public fun `test add duplicate garden`() = runBlocking<Unit> {
		dataSource.addGarden(TestConstants.newGarden)

		try
		{
			dataSource.addGarden(TestConstants.newGarden)
			throw Exception()
		}
		catch (e: Exception)
		{
			e.`should be instance of`(IllegalArgumentException::class)
		}
	}

	@Test
	public fun `test edit garden`() = runBlocking<Unit> {
		val gardens1 = dataSource.getGardens()
		val gardens2 = dataSource.addGarden(TestConstants.newGarden)

		// saving should save, but not re-load from disk
		gardens1.`should be equal to`(gardens2)
		gardens2.`should not be empty`()

		val garden = dataSource.getGardenById(TestConstants.newGarden.id)
		garden
			.`should not be null`()

		val updateString = "Updated name"
		garden.name = updateString

		// garden in backing list should be updated as [garden] should be an object reference
		with (dataSource.getGardenById(garden.id)) {
			this.`should not be null`()
			name.`should be`(updateString)
		}
	}

	@Test
	public fun `test load from disk`() = runBlocking<Unit> {
		dataSource.addGarden(TestConstants.newGarden)
		dataSource.sync(GardensDataSource.SyncDirection.LOAD)
		val garden = dataSource.getGardenById(TestConstants.newGarden.id)
			.`should not be null`()

		val updateString = "Updated name"
		garden.name = updateString

		dataSource.sync(GardensDataSource.SyncDirection.LOAD)

		// we didn't save so the garden should not be updated
		val garden2 = dataSource.getGardenById(TestConstants.newGarden.id)
		garden2.`should not be null`()
		garden2.hashCode().`should not be equal to`(garden.hashCode())
		garden2.name.`should not be equal to`(updateString)
	}
}
