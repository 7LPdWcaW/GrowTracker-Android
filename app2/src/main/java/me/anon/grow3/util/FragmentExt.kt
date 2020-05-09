package me.anon.grow3.util

import androidx.fragment.app.Fragment
import me.anon.grow3.BaseApplication

/**
 * // TODO: Add class description
 */
public val Fragment.component get() = (requireContext().applicationContext as BaseApplication).appComponent
