
package me.anon.grow3.di

import dagger.Component
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.di.module.AppModule
import me.anon.grow3.di.module.DispatcherModule
import me.anon.grow3.ui.crud.activity.CropActivity
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.crud.fragment.DiaryCompleteFragment
import me.anon.grow3.ui.crud.fragment.DiaryCropsFragment
import me.anon.grow3.ui.crud.fragment.DiaryDetailsFragment
import me.anon.grow3.ui.crud.fragment.DiaryEnvironmentFragment
import me.anon.grow3.ui.diaries.fragment.DiariesListFragment
import me.anon.grow3.util.handler.ExceptionHandler
import javax.inject.Singleton

@Singleton
@Component(modules = [
	DispatcherModule::class,
	AppModule::class
])
interface ApplicationComponent
{
	public fun exceptionHandler(): ExceptionHandler
	public fun gardenRepo(): DiariesRepository

	public fun inject(fragment: DiariesListFragment)
	public fun inject(fragment: DiaryDetailsFragment)
	public fun inject(fragment: DiaryCropsFragment)
	public fun inject(fragment: DiaryEnvironmentFragment)
	public fun inject(fragment: DiaryCompleteFragment)

	public fun inject(activity: CropActivity)
	public fun inject(activity: DiaryActivity)
}
