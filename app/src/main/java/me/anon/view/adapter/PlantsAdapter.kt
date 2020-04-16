	package me.anon.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow.R
import me.anon.lib.ext.inflate
import me.anon.model.Plant
import me.anon.view.adapter.viewholder.StandardPlantViewHolder
import me.anon.view.viewmodel.PlantListViewModel

	/**
 * // TODO: Add class description
 */
class PlantsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
	companion object
	{
		public const val TYPE_HIDDEN = 0
		public const val TYPE_COMPACT = 1
		public const val TYPE_STANDARD = 2
		public const val TYPE_EXTREME = 3
	}

	public var items: List<Plant> = arrayListOf()
		set (value) {
			field = value

			filterItems()

			notifyDataSetChanged()
		}

	private var filteredItems = arrayListOf<String>()
	private fun filterItems()
	{
		filteredItems.clear()
		for (item in items)
		{
			if (!filters.any { it.stage == item.stage })
			{
				filteredItems.add(item.id)
			}
		}
	}

	public var filters: List<PlantListViewModel.Filter> = arrayListOf()
		set (value) {
			field = value

			filterItems()

			notifyDataSetChanged()
		}

	override fun getItemCount(): Int = items.size

	override fun getItemViewType(position: Int): Int
	{
		val item = items[position]

		if (filteredItems.contains(item.id)) return TYPE_HIDDEN
		return TYPE_STANDARD
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
	{
		return when (viewType)
		{
			TYPE_HIDDEN -> object : RecyclerView.ViewHolder(parent.inflate(R.layout.empty)) {}
			TYPE_STANDARD -> StandardPlantViewHolder(parent.inflate(R.layout.plant_original_item))
			else -> throw IllegalArgumentException("Invalid view holder type")
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
	{
		when (holder)
		{
			is StandardPlantViewHolder -> holder.bind(items[position])
		}
	}
}
