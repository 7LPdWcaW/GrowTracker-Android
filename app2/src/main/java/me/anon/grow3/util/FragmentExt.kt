package me.anon.grow3.util

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import me.anon.grow3.BaseApplication
import me.anon.grow3.ui.main.activity.MainActivity

public val Fragment.component get() = (requireContext().applicationContext as BaseApplication).appComponent
public val Fragment.navigationPager: ViewPager2? get() = (requireActivity() as? MainActivity)?.viewPager

public inline fun <reified T : Activity> Fragment.navigateTo(block: Intent.() -> Unit = {})
	= startActivity(Intent(requireContext(), T::class.java).apply(block))

public inline fun <reified T : Activity> Fragment.navigateForResult(block: Intent.() -> Unit = {})
	= this.navigateForResult<T>(code<T>(), block)

public inline fun <reified T : Activity> Fragment.navigateForResult(requestCode: Int, block: Intent.() -> Unit = {})
	= startActivityForResult(Intent(requireContext(), T::class.java).apply(block), requestCode)

public fun Fragment.applyWindowInsets(vararg view: View = arrayOf(requireView()), apply: (v: View, left: Int, top: Int, right: Int, bottom: Int) -> Unit)
{
	((requireActivity() as? MainActivity)?.viewPager ?: requireActivity().findViewById(android.R.id.content)).setOnApplyWindowInsetsListener { v, insets ->
		v.onApplyWindowInsets(insets).also {
			view.forEach {
				apply(it, insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
				it.dispatchApplyWindowInsets(insets)
			}
		}
		insets.consumeSystemWindowInsets()
	}
}

// Resource convenience methods
public fun Fragment.dimen(@DimenRes resId: Int): Float = resources.getDimension(resId)
public fun Fragment.dimenPx(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)
public fun Fragment.string(@StringRes resId: Int): String = resId.string(requireContext())

@ColorInt
public fun Fragment.color(@ColorRes resId: Int): Int = resId.color(requireContext())

@ColorInt
public fun Fragment.resColor(@AttrRes resId: Int): Int = resId.resColor(requireContext())

public fun Fragment.drawable(@DrawableRes resId: Int, @ColorInt tint: Int? = null): Drawable = resId.drawable(requireContext(), tint)
