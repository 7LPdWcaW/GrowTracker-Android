package me.anon.grow.fragment

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.HORIZONTAL
import kotlinx.android.synthetic.main.schedule_list_view.*
import me.anon.controller.adapter.FeedingScheduleAdapter
import me.anon.grow.FeedingScheduleDetailsActivity
import me.anon.grow.R
import me.anon.lib.manager.ScheduleManager

/**
 * Fragment for displaying list of feeding schedules
 */
class FeedingScheduleListFragment : Fragment()
{
	private val adapter = FeedingScheduleAdapter()

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater?.inflate(R.layout.schedule_list_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		adapter.items = ScheduleManager.instance.schedules
		recycler_view.adapter = adapter
		recycler_view.layoutManager = LinearLayoutManager(activity)
		recycler_view.addItemDecoration(DividerItemDecoration(activity, HORIZONTAL))

		fab_add.setOnClickListener {
			startActivity(Intent(activity, FeedingScheduleDetailsActivity::class.java))
		}
	}
}
