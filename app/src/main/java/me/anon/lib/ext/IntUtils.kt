package me.anon.lib.ext

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

public fun String?.toSafeInt(): Int
{
	try
	{
		if (this?.indexOf('.') ?: -1 > -1)
		{
			return this?.toDouble()?.toInt() ?: 0
		}

		return this?.toInt() ?: 0
	}
	catch (e: Exception)
	{
		return 0
	}
}

/**
 * Resolves an attribute to an int, or -1 if it failed
 */
public fun @receiver:AttrRes Int.resolveInt(themedContext: Context?): Int
{
	val outValue = TypedValue()
	themedContext?.theme?.resolveAttribute(this, outValue, true) ?: return -1
	return outValue.data
}

/**
 * Resolves an attribute to a res, or -1 if it failed
 */
public fun @receiver:AttrRes Int.resolveRes(themedContext: Context?): Int
{
	val outValue = TypedValue()
	themedContext?.theme?.resolveAttribute(this, outValue, true) ?: return -1
	return outValue.resourceId
}

/**
 * Resolves a colour int from an attr res. Will only work for attr type color/reference (to color res)
 */
@ColorInt
public fun @receiver:AttrRes Int.resolveColor(themedContext: Context?): Int = this.resolveInt(themedContext)

/**
 * Resolves a colour int from an color res.
 */
@ColorInt
public fun @receiver:ColorRes Int.getColor(themedContext: Context): Int = themedContext.resources.getColor(this, themedContext.theme)

/**
 * Resolves a drawable object from a drawable res
 */
public fun @receiver:DrawableRes Int.resolveDrawable(context: Context): Drawable?
{
	return ResourcesCompat.getDrawable(context.resources, this, context.theme)
}
