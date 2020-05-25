package me.anon.grow3.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.*

@AttrRes
public fun @receiver:ColorInt Int.resColor(context: Context): Int
{
	val outValue = TypedValue()
	context.theme?.resolveAttribute(this, outValue, true) ?: return -1
	return outValue.data
}

@ColorInt
public fun @receiver:ColorRes Int.color(context: Context): Int
	= context.resources.getColor(this)

public fun @receiver:DrawableRes Int.drawable(context: Context, @ColorInt tint: Int? = null): Drawable
	= context.resources.getDrawable(this, context.theme)!!.apply {
		tint?.let { setTint(it) }
	}

public fun @receiver:StringRes Int.string(context: Context, vararg params: String = arrayOf()): String
	= context.getString(this, params)
