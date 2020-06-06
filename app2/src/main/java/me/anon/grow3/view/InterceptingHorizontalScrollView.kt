package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

/**
 * [HorizontalScrollView] implementation that intercepts touch events from
 * its parent so that it can be embedded in other horizontally scrolling view
 * groups.
 *
 * @author brandon
 */
class InterceptingHorizontalScrollView : HorizontalScrollView
{
	/**
	 * Constructor.
	 */
	constructor(context: Context?) : super(context)
	{
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
	{
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
	{
	}

	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean
	{
		if (parent != null)
		{
			when (ev.action)
			{
				MotionEvent.ACTION_MOVE -> parent.requestDisallowInterceptTouchEvent(true)
				MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
			}
		}

		return super.onInterceptTouchEvent(ev)
	}
}
