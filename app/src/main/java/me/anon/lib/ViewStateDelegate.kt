package me.anon.lib

import android.content.Context
import android.view.View
import androidx.annotation.IntRange

/**
 * Delegate helper class for managing generic view states.
 */
class ViewStateDelegate(val context: Context)
{
	companion object
	{
		public const val STATE_NORMAL = 1
		public const val STATE_EMPTY = 5
	}

	private val viewRefs = hashMapOf<Int, ArrayList<View>>()
	private val viewQueries = hashMapOf<Int, () -> Boolean>()
	private val stateCache = hashMapOf<Int, List<Int>>()

	/**
	 * Clears out all view references and state cache to prevent hard references blocking GC.
	 * Call this in [Activity.onDestory]
	 */
	public fun disconnect()
	{
		viewRefs.clear()
		stateCache.clear()
	}

	/**
	 * Adds a list of views associated with a specific state. When that state is set, all other views will
	 * be set to [View.GONE] while the associated views will be set to [View.VISIBLE]
	 */
	@JvmName("addViewStateViewsWithQuery")
	@JvmOverloads
	public fun addViewState(@IntRange(from = 1, to = Long.MAX_VALUE) state: Int, views: ArrayList<View>, query: (() -> Boolean)? = null)
	{
		viewRefs[state] = views
		query?.let { viewQueries[state] = it }
		stateCache[state] = views.map { it.visibility }
	}

	/**
	 * Sets the visibility of all views back to their original states
	 */
	public fun reset()
	{
		viewRefs.forEach { (k, v) ->
			val cache = stateCache[k]
			cache?.let {
				v.forEachIndexed { index, view ->
					view.visibility = cache[index]
				}
			}
		}
	}

	/**
	 * Toggles state based on view queries. Ensure only one query will pass else you may end up with
	 * odd view states.
	 */
	public fun setState()
	{
		viewQueries.forEach { (k, v) ->
			if (v())
			{
				setState(k)
			}
		}
	}

	/**
	 * Sets the state for the view cache. Passing 0 will reset the views back to their original states.
	 * Passing -1 will hide all views.
	 */
	public fun setState(state: Int)
	{
		when (state)
		{
			-1 -> {
				viewRefs.forEach { (k, v) ->
					v.forEach { view ->
						view.visibility = View.GONE
					}
				}
			}
			0 -> reset()
			else -> {
				if (viewRefs.containsKey(state))
				{
					viewRefs.forEach { (k, v) ->
						v.forEachIndexed { _, view ->
							view.visibility = if (state == k) View.VISIBLE else View.GONE
						}
					}
				}
			}
		}
	}
}
