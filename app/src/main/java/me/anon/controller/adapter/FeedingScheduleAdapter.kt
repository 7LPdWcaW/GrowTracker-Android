package me.anon.controller.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.anon.grow.R
import me.anon.model.FeedingSchedule
import me.anon.view.ScheduleHolder

/**
 * Adapter for feeding schedule list
 */
class FeedingScheduleAdapter : RecyclerView.Adapter<ScheduleHolder>()
{
	public var onDeleteCallback: (schedule: FeedingSchedule) -> Unit = {}
	public var onCopyCallback: (schedule: FeedingSchedule) -> Unit = {}
	public var items: ArrayList<FeedingSchedule> = arrayListOf()
		set(value)
		{
			items.clear()
			items.addAll(value)
			notifyDataSetChanged()
		}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleHolder
		= ScheduleHolder(this, LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false))

	override fun getItemCount(): Int = items.size

	override fun onBindViewHolder(holder: ScheduleHolder, position: Int)
	{
		holder.bind(items[position])
	}
}
