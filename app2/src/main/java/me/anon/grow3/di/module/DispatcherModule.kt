package me.anon.grow3.di.module

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import me.anon.grow3.di.IoDispatcher
import me.anon.grow3.di.MainDispatcher

@Module
class DispatcherModule
{
	@Provides
	@IoDispatcher
	public fun provideIODispatcher() = Dispatchers.IO

	@Provides
	@MainDispatcher
	public fun provideMainDispatcher() = Dispatchers.Main
}
