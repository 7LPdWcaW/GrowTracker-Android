package me.anon.grow3.di.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.repository.impl.DefaultDiariesRepository
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
	public fun provideContext(): Context = appContext.applicationContext

	@Provides
	@Named("diaries_source")
	public fun provideDiariesSource(context: Context): String
		= context.application.dataPath + "/diaries.db"

	@Provides
	@Named("core_prefs")
	public fun provideCorePrefs(context: Context): SharedPreferences
		= context.getSharedPreferences("core_prefs", Context.MODE_PRIVATE)

	@Provides
	public fun provideDiariesRepository(source: NitriteDiariesDataSource): DiariesRepository
		= DefaultDiariesRepository.getInstance(source)
}
