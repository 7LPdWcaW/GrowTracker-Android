package me.anon.view

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.schedule_item.view.*
import me.anon.grow.FeedingScheduleDetailsActivity
import me.anon.lib.manager.ScheduleManager
import me.anon.model.FeedingSchedule

/**
 * // TODO: Add class description
 */
class ScheduleHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val title = itemView.title
	private val summary = itemView.summary

	public fun bind(feedingSchedule: FeedingSchedule)
	{
		title.text = feedingSchedule.name
		summary.text = feedingSchedule.description

		itemView.setOnClickListener {
			it.context.startActivity(Intent(it.context, FeedingScheduleDetailsActivity::class.java).also {
				it.putExtra("schedule_index", ScheduleManager.instance.schedules.indexOf(feedingSchedule))
			})
		}
	}
}
