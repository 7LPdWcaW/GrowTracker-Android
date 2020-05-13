package me.anon.grow3.di

import dagger.Component
import me.anon.grow3.data.repository.DiariesRepository
import me.anon.grow3.di.module.AppModule
import me.anon.grow3.di.module.DispatcherModule
import me.anon.grow3.ui.gardens.GardenListFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [
	DispatcherModule::class,
	AppModule::class
])
interface ApplicationComponent
{
	public fun gardenRepo(): DiariesRepository

	public fun inject(fragment: GardenListFragment)
}
