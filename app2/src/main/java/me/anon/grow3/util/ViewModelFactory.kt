package me.anon.grow3.util

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

open class ViewModelProvider<out V : ViewModel>(
	private val viewModelFactory: ViewModelFactory<V>,
	owner: SavedStateRegistryOwner,
	defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs)
{
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(
		key: String,
		modelClass: Class<T>,
		handle: SavedStateHandle
	): T
	{
		return viewModelFactory.create(handle) as T
	}
}

interface ViewModelFactory<T : ViewModel>
{
	fun create(handle: SavedStateHandle): T
}
