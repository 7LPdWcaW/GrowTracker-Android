package me.anon.grow3.util

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import androidx.fragment.app.Fragment
import me.anon.grow3.BaseApplication
import me.anon.grow3.R

public val Fragment.component get() = (requireContext().applicationContext as BaseApplication).appComponent

public inline fun <reified T : Activity> Fragment.navigateTo(block: Intent.() -> Unit = {})
	= startActivity(Intent(requireContext(), T::class.java).apply(block))

public inline fun <reified T : Activity> Fragment.navigateForResult(block: Intent.() -> Unit = {})
	= this.navigateForResult<T>(code<T>(), block)

public inline fun <reified T : Activity> Fragment.navigateForResult(requestCode: Int, block: Intent.() -> Unit = {})
	= startActivityForResult(Intent(requireContext(), T::class.java).apply(block), requestCode)

public fun Fragment.applyWindowInsets(view: View = requireView(), top: Boolean = true, bottom: Boolean = true)
{
	requireActivity().findViewById<View>(R.id.view_pager)?.setOnApplyWindowInsetsListener { v, insets ->
		v.onApplyWindowInsets(insets).also {
			view.addSystemWindowInsetToMargin(top = top, bottom = bottom)
			view.dispatchApplyWindowInsets(insets)
		}
	}
}

public fun Fragment.applyWindowPaddings(view: View = requireView(), top: Boolean = true, bottom: Boolean = true)
{
	requireActivity().findViewById<View>(R.id.view_pager)?.setOnApplyWindowInsetsListener { v, insets ->
		v.onApplyWindowInsets(insets).also {
			view.addSystemWindowInsetToPadding(top = top, bottom = bottom)
			view.dispatchApplyWindowInsets(insets)
		}
	}
}

// Resource convenience methods
public fun Fragment.dimension(@DimenRes resId: Int): Float = resources.getDimension(resId)
public fun Fragment.dimensionPixels(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)
public fun Fragment.string(@StringRes resId: Int): String = resId.string(requireContext())

@ColorInt
public fun Fragment.color(@ColorRes resId: Int): Int = resId.color(requireContext())

@ColorInt
public fun Fragment.resColor(@AttrRes resId: Int): Int = resId.resColor(requireContext())

public fun Fragment.drawable(@DrawableRes resId: Int, @ColorInt tint: Int? = null): Drawable = resId.drawable(requireContext(), tint)
