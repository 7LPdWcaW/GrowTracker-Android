package me.anon.view.adapter.viewholder

import android.content.Intent
import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.plant_extreme_item.view.*
import me.anon.model.Plant
import me.anon.view.PlantDetailsActivity2

/**
 * // TODO: Add class description
 */
class StandardPlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val image = itemView.image
	private val name = itemView.name
	private val summary = itemView.summary

	public fun bind(plant: Plant)
	{
		name.text = plant.name
		val summaryList = plant.generateSummary(itemView.context, 2)
		summary.text = Html.fromHtml(TextUtils.join("<br />", summaryList))

		itemView.setOnClickListener {
			it.context.startActivity(Intent(it.context, PlantDetailsActivity2::class.java).apply {
				putExtra("plantId", plant.id)
			})
		}
	}
}
