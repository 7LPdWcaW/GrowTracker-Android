package me.anon.grow3.util

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import me.anon.grow3.BaseApplication

public val Fragment.component get() = (requireContext().applicationContext as BaseApplication).appComponent
public inline fun <reified T : Activity> Fragment.navigateTo(block: Intent.() -> Unit = {}) = startActivity(Intent(requireContext(), T::class.java).apply(block))
