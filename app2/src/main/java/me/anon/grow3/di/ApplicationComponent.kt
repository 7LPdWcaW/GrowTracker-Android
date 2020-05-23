package me.anon.grow3.di

import dagger.Component
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.di.module.AppModule
import me.anon.grow3.di.module.DispatcherModule
import me.anon.grow3.ui.crud.fragment.DiaryDetailsFragment
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
}
