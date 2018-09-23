package me.anon.grow.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow.R

/**
 * // TODO: Add class description
 */
class FeedingScheduleListFragment : Fragment()
{
	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater?.inflate(R.layout.schedule_list_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
	}
}
