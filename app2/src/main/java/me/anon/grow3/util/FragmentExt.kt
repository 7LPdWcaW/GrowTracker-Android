package me.anon.grow3.util

import androidx.fragment.app.Fragment
import me.anon.grow3.BaseApplication
import me.anon.grow3.di.ApplicationComponent

/**
 * // TODO: Add class description
 */
public val Fragment.appComponent get() = (requireContext().applicationContext as BaseApplication).appComponent

public inline fun Fragment.inject() {
	ApplicationComponent.autoInject(appComponent, this)
}
