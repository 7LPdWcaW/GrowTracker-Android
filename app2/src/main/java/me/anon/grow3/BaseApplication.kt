package me.anon.grow3

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.di.DaggerApplicationComponent
import me.anon.grow3.di.module.AppModule
import timber.log.Timber

abstract class BaseApplication : Application()
{
	// todo: change this to pref to inject
	public var dataPath: String = ""

	public lateinit var appComponent: ApplicationComponent

	override fun onCreate()
	{
		super.onCreate()

		AndroidThreeTen.init(this)
		Timber.plant(Timber.DebugTree())

		dataPath = getExternalFilesDir(null)!!.absolutePath
		setup()

		appComponent = DaggerApplicationComponent.builder()
			.appModule(AppModule(this))
			.build()
	}

	abstract fun setup()
}
