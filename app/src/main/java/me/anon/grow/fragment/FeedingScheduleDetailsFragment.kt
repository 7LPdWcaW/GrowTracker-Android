package me.anon.grow.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.schedule_details_view.*
import me.anon.grow.R
import me.anon.lib.manager.ScheduleManager
import me.anon.model.FeedingSchedule
import me.anon.model.FeedingScheduleDate

/**
 * // TODO: Add class description
 */
class FeedingScheduleDetailsFragment : Fragment()
{
	companion object
	{
		fun newInstance(scheduleIndex: Int = -1): FeedingScheduleDetailsFragment
		{
			return FeedingScheduleDetailsFragment().apply {
				this.scheduleIndex = scheduleIndex
			}
		}
	}

	private var scheduleIndex: Int = -1
	private var schedules = arrayListOf<FeedingScheduleDate>()

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater?.inflate(R.layout.schedule_details_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		if (scheduleIndex > -1)
		{
			schedules = ScheduleManager.instance.schedules[scheduleIndex].schedules
			name.setText(ScheduleManager.instance.schedules[scheduleIndex].name)
			description.setText(ScheduleManager.instance.schedules[scheduleIndex].description)
			populateSchedules()
		}

		fab_complete.setOnClickListener {
			when (scheduleIndex)
			{
				-1 -> {
					val schedule: FeedingSchedule = FeedingSchedule(
						name = name.text.toString(),
						description = description.text.toString(),
						schedules = schedules
					)

					ScheduleManager.instance.schedules.add(schedule)
				}
				else -> {
					ScheduleManager.instance.schedules[scheduleIndex].apply {
						name = this@FeedingScheduleDetailsFragment.name.text.toString()
						description = this@FeedingScheduleDetailsFragment.description.text.toString()
						schedules = this@FeedingScheduleDetailsFragment.schedules
					}
				}
			}
		}
	}

	/**
	 * Populates the schedules container
	 */
	private fun populateSchedules()
	{
		
	}
}
