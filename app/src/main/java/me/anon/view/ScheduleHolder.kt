package me.anon.view

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import me.anon.controller.adapter.FeedingScheduleAdapter
import me.anon.grow.FeedingScheduleDetailsActivity
import me.anon.grow.R
import me.anon.grow.databinding.ScheduleItemBinding
import me.anon.model.FeedingSchedule

/**
 * Feeding schedule view holder class
 */
class ScheduleHolder(val adapter: FeedingScheduleAdapter, itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private var binding = ScheduleItemBinding.bind(itemView)
	private val title = binding.title
	private val summary = binding.summary
	private val delete = binding.delete
	private val copy = binding.copy

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
				it.putExtra("schedule", feedingSchedule)
			})
		}
	}
}
