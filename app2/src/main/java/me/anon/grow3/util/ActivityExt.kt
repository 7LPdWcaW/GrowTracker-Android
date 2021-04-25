package me.anon.grow3.util

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*

public inline fun <reified T : Activity> Activity.newTask(block: Intent.() -> Unit = {})
	= startActivity(Intent(this, T::class.java).apply(block))

public fun Activity.clearFocus()
{
	if (currentFocus?.focusSearch(View.FOCUS_RIGHT)?.requestFocus() != true) currentFocus?.clearFocus()
}

// Resource convenience methods
public fun Activity.dimension(@DimenRes resId: Int): Float = resources.getDimension(resId)
public fun Activity.dimensionPixels(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)
public fun Activity.string(@StringRes resId: Int): String = resId.string(this)

@ColorInt
public fun Activity.color(@ColorRes resId: Int): Int = resId.color(this)

@ColorInt
public fun Activity.resColor(@AttrRes resId: Int): Int = resId.resColor(this)

public fun Activity.drawable(@DrawableRes resId: Int, @ColorInt tint: Int? = null): Drawable = resId.drawable(this, tint)
