	package me.anon.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow.R
import me.anon.lib.ext.inflate
import me.anon.model.Plant
import me.anon.view.adapter.viewholder.StandardPlantViewHolder

/**
 * // TODO: Add class description
 */
class PlantsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
	public var items: List<Plant> = arrayListOf()
		set (value) {
			field = value
			notifyDataSetChanged()
		}

	override fun getItemCount(): Int = items.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
	{
		return StandardPlantViewHolder(parent.inflate(R.layout.plant_original_item))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
	{
		when (holder)
		{
			is StandardPlantViewHolder -> holder.bind(items[position])
		}
	}
}
