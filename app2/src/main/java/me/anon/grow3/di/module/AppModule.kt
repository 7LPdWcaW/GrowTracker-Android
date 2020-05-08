package me.anon.grow3.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import me.anon.grow3.data.repository.GardensRepository
import me.anon.grow3.data.repository.impl.DefaultGardensRepository
import me.anon.grow3.data.source.GardensDataSource
import me.anon.grow3.data.source.json.JsonGardensDataSource
import me.anon.grow3.util.application
import javax.inject.Named
import javax.inject.Singleton

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
	@Singleton
	public fun provideGardenDataSource(@Named("garden_source") path: String): GardensDataSource = JsonGardensDataSource(path)

	@Provides
	@Singleton
	public fun provideGardenRepository(dataSource: GardensDataSource): GardensRepository = DefaultGardensRepository(dataSource)
}
