package me.anon.controller.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.anon.grow.R
import me.anon.model.FeedingScheduleDate
import me.anon.model.Plant
import me.anon.model.PlantStage
import me.anon.view.FeedingDateHolder
import java.util.*

/**
 * // TODO: Add class description
 */
class FeedingDateAdapter : RecyclerView.Adapter<FeedingDateHolder>()
{
	public var onItemSelectCallback: (date: FeedingScheduleDate) -> Unit = {}
	public var items: ArrayList<FeedingScheduleDate> = arrayListOf()
		set(value)
		{
			items.clear()
			items.addAll(value)
			notifyDataSetChanged()
		}
	public var plant: Plant = Plant()
	public val plantStages: SortedMap<PlantStage, Long> by lazy { plant.calculateStageTime() }

	public fun getLastStage(): PlantStage = plantStages.toSortedMap().lastKey()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedingDateHolder
		= FeedingDateHolder(this, LayoutInflater.from(parent.context).inflate(R.layout.feeding_date_stub, parent, false))

	override fun getItemCount(): Int = items.size

	override fun onBindViewHolder(holder: FeedingDateHolder, position: Int)
	{
		holder.bind(items[position])
	}
}
