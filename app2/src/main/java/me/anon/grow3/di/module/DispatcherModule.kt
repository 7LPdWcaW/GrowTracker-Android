package me.anon.grow3.di.module

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Named

@Module
class DispatcherModule
{
	@Provides
	@Named("io_dispatcher")
	public fun provideIODispatcher() = Dispatchers.IO

	@Provides
	@Named("main_dispatcher")
	public fun provideMainDispatcher() = Dispatchers.Main
}
