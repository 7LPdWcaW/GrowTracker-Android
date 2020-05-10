package me.anon.grow3.di

import dagger.Component
import me.anon.grow3.data.repository.GardensRepository
import me.anon.grow3.di.module.AppModule
import me.anon.grow3.di.module.DispatcherModule
import me.anon.grow3.ui.gardens.GardenListFragment
import javax.inject.Singleton

/**
 * // TODO: Add class description
 */
@Singleton
@Component(modules = [
	DispatcherModule::class,
	AppModule::class
])
interface ApplicationComponent
{
	public fun gardenRepo(): GardensRepository

	public fun inject(fragment: GardenListFragment)
}
