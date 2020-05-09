package me.anon.grow3.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import me.anon.grow3.data.repository.GardensRepository
import me.anon.grow3.data.repository.impl.DefaultGardensRepository
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.data.source.json.JsonGardensDataSource
import me.anon.grow3.util.application
import javax.inject.Named

/**
 * // TODO: Add class description
 */
@Module
class AppModule(
	private val appContext: Context
)
{
	@Provides public fun provideAppContext() = appContext.applicationContext

	@Provides
	@Named("garden_source")
	public fun provideGardenSource(): String = appContext.application.dataPath

	@Provides
	@Named("io_dispatcher")
	public fun provideDispatcher() = Dispatchers.IO

	@Provides
	public fun provideGardenDataSource(dataSource: JsonGardensDataSource): GardensDataSource = dataSource

	@Provides
	public fun provideGardenRepository(repo: DefaultGardensRepository): GardensRepository = repo
}
