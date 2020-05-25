package me.anon.grow3.util

import android.app.Activity
import android.content.Intent

public inline fun <reified T : Activity> Activity.navigateTo(block: Intent.() -> Unit = {})
	= startActivity(Intent(this, T::class.java).apply(block))

