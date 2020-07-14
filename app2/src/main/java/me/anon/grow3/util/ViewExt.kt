package me.anon.grow3.util

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.*
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow3.ui.base.BaseFragment

public inline fun <reified T : BaseFragment> View.navigateTo(ontop: Boolean = false, arguments: () -> Bundle? = { null })
	= findFragment<BaseFragment>().navigateTo<T>(ontop, arguments)

/**
 * Creates a click listener for generic type extending [View] and passes as a typed argument
 * in the callback
 */
public inline fun <reified T : View> T.onClick(crossinline listener: (T) -> Unit): T
{
	setOnClickListener { listener(it as T) }
	return this
}

/**
 * Creates a click listener for generic type extending [View] and passes as a typed argument
 * in the callback
 */
public inline fun <reified T : View> T.onLongClick(crossinline listener: (T) -> Boolean): T
{
	setOnLongClickListener { listener(it as T) }
	return this
}

/**
 * Creates a on focus listener. for generic type extending [View] and passes as a typed argument
 * in the callback. This will only be called when the view is focused
 */
public inline fun <reified T : View> T.onFocus(crossinline listener: (T) -> Unit): T
{
	setOnFocusChangeListener { v, hasFocus ->
		if (v == this && hasFocus) listener(v as T)
	}
	return this
}

/**
 * Creates a on focus listener. for generic type extending [View] and passes as a typed argument
 * in the callback. This will only be called when the view is focused
 */
public inline fun <reified T : View> T.onFocusLoss(crossinline listener: (T) -> Unit): T
{
	setOnFocusChangeListener { v, hasFocus ->
		if (v == this && !hasFocus) listener(v as T)
	}
	return this
}

/**
 * Convenience method for inflating a view into another. Will return the inflated view, or the parent view if attach = true
 */
public fun <T : View> View.inflate(@LayoutRes layoutRes: Int, attach: Boolean = false): T
{
	@Suppress("UNCHECKED_CAST")
	return LayoutInflater.from(context).inflate(layoutRes, this as ViewGroup, attach) as T
}

/**
 * Hides the keyboard from a given window view
 */
public fun View.hideKeyboard()
{
	val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
	imm?.let {
		it.hideSoftInputFromWindow(windowToken, 0);
    }
}

/**
 * Shows implicitly the keyboard from a given window view
 */
public fun View.showKeyboard()
{
	val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
	imm?.let {
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}

/**
 * Finds all child views from a given view by instance of class
 */
public fun <T : View> View.findChildrenByClass(t: Class<T>): ArrayList<T>
{
	val returnList = arrayListOf<T>()

	if (this is ViewGroup)
	{
		if (t.isAssignableFrom(this::class.java))
		{
			returnList.add(this as T)
		}
		else
		{
			val count = childCount
			for (index in 0 until count)
			{
				val view = getChildAt(index)
				if (view is ViewGroup) returnList.addAll(view.findChildrenByClass(t))
				else if (t.isAssignableFrom(view!!::class.java)) returnList.add(view as T)
			}
		}
	}

	return returnList
}

/**
 * Removes all views from the start index
 */
public fun ViewGroup.removeViewsFrom(start: Int = 0)
{
	val count = childCount - start - 1
	if (count in 1 until childCount)
	{
		removeViews(start, childCount - start - 1)
	}
}

public inline fun <reified T> View.parentViewByInstance(): T
{
	var parent: View? = parentView
	while (parent != null)
	{
		if (parent::class is T) return parent as T
		else
		{
			if (parentView.id == android.R.id.content) parent = null
			else parent = parent.parentView
		}
	}

	throw IllegalArgumentException("View of type ${T::class} was not found")
}

public fun <T> View.parentViewById(@IdRes id: Int): T
{
	var parent: View? = parentView
	while (parent != null)
	{
		if (parent.id == id) return parent as T
		else
		{
			if (parentView.id == android.R.id.content) parent = null
			else parent = parent.parentView
		}
	}

	throw IllegalArgumentException("View with id $id was not found")
}

/**
 * Returns a sequence of child views from a given view.
 * If the view is not a [ViewGroup], an empty sequence will be returned
 */
public val View.childViews: Sequence<View>
	get() {
		return when {
			(this is ViewGroup) -> {
				var index = 0
				generateSequence {
					getChildAt(index++)
				}
			}
			else -> sequenceOf()
		}
	}

/**
 * Gets a view parent as a casted view
 */
public val <T : View> T.parentView: View
	get() = this.parent as View

/**
 * Lambda extension for waiting for a view to measure
 */
inline fun <T : View> T.afterMeasured(crossinline callback: T.() -> Unit)
{
	addOnLayoutChangeListener(object : View.OnLayoutChangeListener
	{
		override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int)
		{
			if (measuredWidth > 0 && measuredHeight > 0)
			{
				removeOnLayoutChangeListener(this)
				callback()
			}
		}
	})
}

public fun View.updateMargin(rect: Rect) = this.updateMargin(rect.left, rect.top, rect.right, rect.bottom)

public fun View.updateMargin(
	left: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0,
	top: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0,
	right: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0,
	bottom: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
)
{
	updateLayoutParams {
		(this as? ViewGroup.MarginLayoutParams)?.let {
			updateMargins(left = left, top = top, right = right, bottom = bottom)
		}
	}
}

public fun View.updateMarginRelative(
	left: Int = 0,
	top: Int = 0,
	right: Int = 0,
	bottom: Int = 0
)
{
	updateLayoutParams {
		val l: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0
		val t: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
		val r: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0
		val b: Int = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0

		(this as? ViewGroup.MarginLayoutParams)?.let {
			updateMargins(left = left + l, top = top + t, right = right + r, bottom = bottom + b)
		}
	}
}

/**
 * Removes all the decorators in a given [RecyclerView]
 */
public fun RecyclerView.removeAllItemDecorators()
{
	for (index in 0 until this.itemDecorationCount)
	{
		removeItemDecorationAt(index)
	}
}

// Resource convenience methods
public fun View.dimen(@DimenRes resId: Int): Float = resources.getDimension(resId)
public fun View.dimenPx(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)
public fun View.string(@StringRes resId: Int): String = resId.string(context)

@ColorInt
public fun View.color(@ColorRes resId: Int): Int = resId.color(context)

@ColorInt
public fun View.resColor(@AttrRes resId: Int): Int = resId.resColor(context)

public fun View.drawable(@DrawableRes resId: Int, @ColorInt tint: Int? = null): Drawable = resId.drawable(context, tint)

public var TextView.drawableStart: Drawable
	set(value) = setCompoundDrawablesWithIntrinsicBounds(value, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3])
	get() = compoundDrawables[0]

public var TextView.drawableTop: Drawable
	set(value )= setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], value, compoundDrawables[2], compoundDrawables[3])
	get() = compoundDrawables[1]

public var TextView.drawableEnd: Drawable
	set(value )= setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1], value, compoundDrawables[3])
	get() = compoundDrawables[2]

public var TextView.drawableBottom: Drawable
	set(value )= setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], value)
	get() = compoundDrawables[3]
