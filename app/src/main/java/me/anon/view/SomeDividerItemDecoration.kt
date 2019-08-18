package me.anon.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

/**
 * Custom implementation for [DividerItemDecoration] that allows a say in which items should have a divider or not
 */
open class SomeDividerItemDecoration(
	open val context: Context,
	open var orientation: Int = VERTICAL,
	open val dividerRes: Int = -1,
	open var showDivider: (position: Int, holder: RecyclerView.ViewHolder, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) -> Boolean = { _, _, _ -> true }
) : RecyclerView.ItemDecoration()
{
	companion object
	{
		public const val HORIZONTAL = LinearLayout.HORIZONTAL
		public const val VERTICAL = LinearLayout.VERTICAL
	}

	private val ATTRS = intArrayOf(android.R.attr.listDivider)
	private val mBounds = Rect()
	public var divider: Drawable

	init
	{
		if (dividerRes != -1)
		{
			this.divider = ResourcesCompat.getDrawable(context.resources, dividerRes, context.theme)!!
		}
		else
		{
			val a = context.obtainStyledAttributes(ATTRS)
			val divider = a.getDrawable(0)
			if (divider == null)
			{
				Log.w("Recycler", "@android:attr/listDivider was not set in the theme used for this DividerItemDecoration. Please set that attribute all call setDrawable()")
			}

			this.divider = divider ?: ColorDrawable(0x000000)

			a.recycle()
		}
	}

	override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State)
	{
		parent.layoutManager?.let {
			when (orientation)
			{
				VERTICAL -> drawVertical(c, parent)
				else -> drawHorizontal(c, parent)
			}
		}
	}

	private fun drawVertical(canvas: Canvas, parent: RecyclerView)
	{
		canvas.save()
		val left: Int
		val right: Int

		if (parent.clipToPadding)
		{
			left = parent.paddingLeft
			right = parent.width - parent.paddingRight
			canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
		}
		else
		{
			left = 0
			right = parent.width
		}

		val childCount = parent.childCount
		for (i in 0 until childCount)
		{
			var itemIndex = parent.getChildAdapterPosition(parent.getChildAt(i))
			var viewHolder = parent.getChildViewHolder(parent.getChildAt(i))
			if (showDivider.invoke(itemIndex, viewHolder, parent.adapter!!))
			{
				val child = parent.getChildAt(i)
				parent.getDecoratedBoundsWithMargins(child, mBounds)
				val bottom = mBounds.bottom + Math.round(child.translationY)
				val top = bottom - divider.intrinsicHeight
				divider.setBounds(left, top, right, bottom)
				divider.draw(canvas)
			}
		}

		canvas.restore()
	}

	private fun drawHorizontal(canvas: Canvas, parent: RecyclerView)
	{
		canvas.save()
		val top: Int
		val bottom: Int

		if (parent.clipToPadding)
		{
			top = parent.paddingTop
			bottom = parent.height - parent.paddingBottom
			canvas.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
		}
		else
		{
			top = 0
			bottom = parent.height
		}

		val childCount = parent.childCount
		for (i in 0 until childCount)
		{
			var itemIndex = parent.getChildAdapterPosition(parent.getChildAt(i))
			var viewHolder = parent.getChildViewHolder(parent.getChildAt(i))
			if (showDivider.invoke(itemIndex, viewHolder, parent.adapter!!))
			{
				val child = parent.getChildAt(i)
				parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
				val right = mBounds.right + Math.round(child.translationX)
				val left = right - divider.intrinsicWidth
				divider.setBounds(left, top, right, bottom)
				divider.draw(canvas)
			}
		}

		canvas.restore()
	}

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State)
	{
		val i = parent.indexOfChild(view)
		var itemIndex = parent.getChildAdapterPosition(parent.getChildAt(i))
		var viewHolder = parent.getChildViewHolder(parent.getChildAt(i))
		if (showDivider.invoke(itemIndex, viewHolder, parent.adapter!!))
		{
			if (orientation == VERTICAL)
			{
				outRect.set(0, 0, 0, divider.intrinsicHeight)
			}
			else
			{
				outRect.set(0, 0, divider.intrinsicWidth, 0)
			}
		}
	}
}
