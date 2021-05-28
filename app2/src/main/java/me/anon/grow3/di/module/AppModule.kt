package me.anon.grow3.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.repository.impl.DefaultDiariesRepository
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.data.source.nitrite.NitriteDiariesDataSource
import me.anon.grow3.util.application
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule(
	private val appContext: Context
)
{
	@Singleton
	@Provides
	public fun provideAppContext() = appContext.applicationContext

	@Singleton
	@Provides
	@Named("garden_source")
	public fun provideGardenSource(): String = appContext.application.dataPath + "/diaries.db"

	@Singleton
	@Provides
	public fun provideDiariesDataSource(dataSource: NitriteDiariesDataSource): DiariesDataSource = dataSource

	@Singleton
	@Provides
	public fun provideDiariesRepository(source: DiariesDataSource): DiariesRepository
		= DefaultDiariesRepository.getInstance(source)
}
