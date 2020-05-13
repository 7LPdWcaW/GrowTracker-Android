package me.anon.grow3.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.TestConstants
import me.anon.grow3.data.repository.impl.DefaultGardensRepository
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.util.DataResult
import me.anon.grow3.util.asSuccess
import me.anon.grow3.util.awaitResult
import me.anon.grow3.util.getOrAwaitValue
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

	private lateinit var dataSource: GardensDataSource
	private lateinit var gardensRepository: DefaultGardensRepository

	@Before
	public fun initialiseRepositories()
	{
		dataSource = FakeGardenDataSource(TestConstants.diaries.toMutableList())
		gardensRepository = DefaultGardensRepository(dataSource)
	}

	@Test
	public fun `test observe garden sends states`()
	{
		val gardens = gardensRepository.observeGardens()
		gardens.getOrAwaitValue().`should be equal to`(DataResult.Loading)
		gardens.getOrAwaitValue().`should not be null`()
			.`should be instance of`(DataResult.Success::class)
		gardens.getOrAwaitValue().asSuccess().`should not be empty`()
	}

	@Test
	public fun `test single livedata observer`() = mainCoroutineRule.runBlockingTest {
		val gardens1 = gardensRepository.observeGardens()
		gardens1.awaitResult().`should be instance of`(DataResult.Success::class)
		val gardens2 = gardensRepository.observeGardens()
		gardens2.awaitResult().`should be instance of`(DataResult.Success::class)

		gardens1.`should be equal to`(gardens2)

		// invalidate the data
		gardensRepository.invalidate()

		val gardens3 = gardensRepository.observeGardens()
		gardens3.`should be equal to`(gardens1).`should be equal to`(gardens2)
	}

	@Test
	public fun `test invalidate discards changes`() = mainCoroutineRule.runBlockingTest {
		val gardens = gardensRepository.observeGardens().awaitResult()
		val garden = gardens.asSuccess().first()
		val changeName = "changed name"
		garden.name = changeName

		// re-set the repository so [invalidate] loads fresh
		initialiseRepositories()
		gardensRepository.invalidate()

		val gardens2 = gardensRepository.observeGardens().awaitResult()
		val garden2 = gardens2.asSuccess().first()
		garden2.name.`should not be equal to`(changeName)
	}

	@Test
	public fun `test observe single garden`() = mainCoroutineRule.runBlockingTest {
		val garden = gardensRepository.observeGarden("abcd-efgh-1234-567890")
		val data = garden.awaitResult().asSuccess()
		data.name = "changed name"
	}
}
