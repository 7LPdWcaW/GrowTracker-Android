package me.anon.view

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.schedule_item.view.*
import me.anon.controller.adapter.FeedingScheduleAdapter
import me.anon.grow.FeedingScheduleDetailsActivity
import me.anon.grow.R
import me.anon.lib.manager.ScheduleManager
import me.anon.model.FeedingSchedule

/**
 * Feeding schedule view holder class
 */
class ScheduleHolder(val adapter: FeedingScheduleAdapter, itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val title = itemView.title
	private val summary = itemView.summary
	private val delete = itemView.delete
	private val copy = itemView.copy

	public fun bind(feedingSchedule: FeedingSchedule)
	{
		title.text = feedingSchedule.name
		summary.text = feedingSchedule.description

		summary.visibility = when {
			summary.text.isEmpty() -> View.GONE
			else -> View.VISIBLE
		}

		delete.setOnClickListener {
			AlertDialog.Builder(it.context)
				.setTitle(R.string.confirm_title)
				.setMessage(R.string.confirm_delete_schedule)
				.setPositiveButton(R.string.confirm_positive) { _, _ ->
					adapter.onDeleteCallback.invoke(feedingSchedule)
				}
				.setNegativeButton(R.string.confirm_negative, null)
				.show()
		}

		copy.setOnClickListener {
			AlertDialog.Builder(it.context)
				.setTitle(R.string.confirm_title)
				.setMessage(R.string.confirm_copy_schedule)
				.setPositiveButton(R.string.confirm_positive) { _, _ ->
					adapter.onCopyCallback.invoke(feedingSchedule)
				}
				.setNegativeButton(R.string.confirm_negative, null)
				.show()
		}

		itemView.setOnClickListener {
			it.context.startActivity(Intent(it.context, FeedingScheduleDetailsActivity::class.java).also {
				it.putExtra("schedule_index", ScheduleManager.instance.schedules.indexOf(feedingSchedule))
			})
		}
	}
}
