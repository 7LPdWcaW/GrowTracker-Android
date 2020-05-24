package me.anon.grow3.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat

@ColorInt
public fun @receiver:ColorRes Int.color(context: Context): Int
	= context.resources.getColor(this)

public fun @receiver:DrawableRes Int.drawable(context: Context): Drawable
	= ResourcesCompat.getDrawable(context.resources, this, context.theme)!!

public fun @receiver:StringRes Int.string(context: Context, vararg params: String = arrayOf()): String
	= context.getString(this, params)
