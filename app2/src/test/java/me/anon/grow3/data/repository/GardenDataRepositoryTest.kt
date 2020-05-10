package me.anon.grow3.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import me.anon.grow3.MainCoroutineRule
import me.anon.grow3.data.repository.impl.DefaultGardensRepository
import me.anon.grow3.data.source.GardensDataSource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * // TODO: Add class description
 */
@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class GardenDataRepositoryTest
{
	// Set the main coroutines dispatcher for unit testing.
	@ExperimentalCoroutinesApi
	@get:Rule
	var mainCoroutineRule = MainCoroutineRule()

	private lateinit var dataSource: GardensDataSource
	private lateinit var gardensRepository: GardensRepository

	@Before
	public fun initialiseRepositories()
	{
		dataSource = FakeGardenDataSource(TestConstants.gardens)
		gardensRepository = DefaultGardensRepository(dataSource)
	}

	@Test
	public fun testGardenObserver() = mainCoroutineRule.runBlockingTest {
		val gardens = gardensRepository.getGardens()
		assert(gardens.isEmpty())
	}
}
