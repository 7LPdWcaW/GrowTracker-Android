package me.anon.view.adapter.viewholder

import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.imageaware.ImageAware
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware
import kotlinx.android.synthetic.main.plant_extreme_item.view.*
import me.anon.grow.R
import me.anon.model.Plant
import me.anon.view.fragment.PlantListFragmentDirections

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

		ImageLoader.getInstance().cancelDisplayTask(image)
		plant.images.lastOrNull().let {
			if (it == null)
			{
				image.setImageResource(R.drawable.default_plant)
			}
			else
			{
				val path = "file://$it"
				val imageAware: ImageAware = ImageViewAware(image, true)
				ImageLoader.getInstance().displayImage(path, imageAware)
			}
		}

		itemView.setOnClickListener {
			it.findNavController().navigate(PlantListFragmentDirections.actionPlantDetails(plant.id))
		}
	}
}
