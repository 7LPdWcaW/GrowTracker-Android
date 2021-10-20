package me.anon.grow3.di.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.data.repository.impl.DefaultDiariesRepository
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.data.source.nitrite.NitriteDiariesDataSource
import me.anon.grow3.di.Cards
import me.anon.grow3.di.CorePrefs
import me.anon.grow3.di.DiariesSource
import me.anon.grow3.ui.common.view.LogMediumCard
import me.anon.grow3.ui.common.view.StagesCard
import me.anon.grow3.ui.crops.view.CropCard
import me.anon.grow3.ui.crops.view.CropDetailsCard
import me.anon.grow3.ui.crops.view.CropLinksCard
import me.anon.grow3.ui.diaries.view.DiaryCropsCard
import me.anon.grow3.ui.diaries.view.DiaryLinksCard
import me.anon.grow3.ui.logs.view.LogDateSeparator
import me.anon.grow3.ui.logs.view.PhotoLogCard
import me.anon.grow3.ui.logs.view.StageChangeLogCard
import me.anon.grow3.ui.logs.view.WaterLogCard
import me.anon.grow3.util.NitriteFacade
import me.anon.grow3.util.application
import me.anon.grow3.view.model.Card
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
	@DiariesSource
	public fun provideDiariesSource(context: Context): String
		= context.application.dataPath + "/diaries.db"

	@Provides
	@CorePrefs
	public fun provideCorePrefs(context: Context): SharedPreferences
		= context.getSharedPreferences("core_prefs", Context.MODE_PRIVATE)

	@Provides
	@Singleton
	public fun provideDiariesDataSource(
		@DiariesSource sourcePath: String,
		nitriteFacade: NitriteFacade,
		@Named("io_dispatcher") dispatcher: CoroutineDispatcher
	): DiariesDataSource
		= NitriteDiariesDataSource(sourcePath, nitriteFacade, dispatcher)

	@Provides
	@Singleton
	public fun provideDiariesRepository(source: DiariesDataSource): DiariesRepository
		= DefaultDiariesRepository.getInstance(source)

	@Provides
	@Singleton
	@Cards
	public fun provideCardsList(): Array<Class<out Card<*>>> = arrayOf(
		// logs
		LogDateSeparator::class.java,
		PhotoLogCard::class.java,
		StageChangeLogCard::class.java,
		WaterLogCard::class.java,

		// common
		LogMediumCard::class.java,
		StagesCard::class.java,

		// crop
		CropCard::class.java,
		CropDetailsCard::class.java,
		CropLinksCard::class.java,

		// diary
		DiaryCropsCard::class.java,
		DiaryLinksCard::class.java,
	)
}
