package me.anon.grow3.di

import androidx.fragment.app.Fragment
import dagger.Component
import me.anon.grow3.di.module.AppModule
import me.anon.grow3.ui.gardens.GardenListFragment
import javax.inject.Singleton

/**
 * // TODO: Add class description
 */
@Singleton
@Component(modules = [
	AppModule::class
])
interface ApplicationComponent
{
	public fun inject(fragment: GardenListFragment)

	companion object
	{
		public fun autoInject(component: ApplicationComponent, injectable: Fragment)
		{
			when (injectable)
			{
				is GardenListFragment -> component.inject(injectable)
			}
		}
	}
}
