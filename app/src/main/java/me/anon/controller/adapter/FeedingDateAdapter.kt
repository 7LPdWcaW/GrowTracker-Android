package me.anon.controller.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.anon.grow.R
import me.anon.model.FeedingScheduleDate
import me.anon.model.Plant
import me.anon.model.PlantStage
import me.anon.view.FeedingDateHolder
import java.util.*
import kotlin.collections.ArrayList

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
	public var plants: ArrayList<Plant> = arrayListOf()
	public val plantStages: ArrayList<SortedMap<PlantStage, Long>> by lazy {
		ArrayList(plants.map { it.calculateStageTime() })
	}

	public fun getLastStages(): ArrayList<PlantStage>
	{
		return ArrayList(plantStages.map {
			it.toSortedMap().lastKey()
		})
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedingDateHolder
		= FeedingDateHolder(this, LayoutInflater.from(parent.context).inflate(R.layout.feeding_date_stub, parent, false))

	override fun getItemCount(): Int = items.size

	override fun onBindViewHolder(holder: FeedingDateHolder, position: Int)
	{
		holder.bind(items[position])
	}
}
