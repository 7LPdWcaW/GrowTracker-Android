package me.anon.lib.ext

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

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

public fun RecyclerView.removeAllItemDecorators()
{
	for (index in 0 until this.itemDecorationCount)
	{
		removeItemDecorationAt(index)
	}
}

// Resource convenience methods
public fun View.dimension(@DimenRes resId: Int): Float = resources.getDimension(resId)
public fun View.dimensionPixels(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)
public fun View.string(@StringRes resId: Int): String = resources.getString(resId)
public fun View.color(@ColorRes resId: Int): Int = resources.getColor(resId)

public var TextView.text: String
	set(value) {
		setText(value)
	}
	get() = text.toString()

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
