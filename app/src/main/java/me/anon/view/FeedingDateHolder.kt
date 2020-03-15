package me.anon.view

import android.text.Html
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.feeding_date_stub.view.*
import me.anon.controller.adapter.FeedingDateAdapter
import me.anon.grow.R
import me.anon.lib.Unit
import me.anon.lib.ext.resolveColor
import me.anon.lib.helper.TimeHelper
import me.anon.model.FeedingScheduleDate
import kotlin.math.floor

/**
 * // TODO: Add class description
 */
class FeedingDateHolder(val adapter: FeedingDateAdapter, itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val card = itemView as MaterialCardView
	private val title = itemView.title
	private val additives = itemView.additives
	private val delete = itemView.delete
	private val copy = itemView.copy

	private val measureUnit: Unit by lazy { Unit.getSelectedMeasurementUnit(itemView.context); }
	private val deliveryUnit: Unit by lazy { Unit.getSelectedDeliveryUnit(itemView.context); }

	public fun bind(feedingSchedule: FeedingScheduleDate)
	{
		delete.visibility = View.GONE
		copy.visibility = View.GONE
		card.setCardBackgroundColor(R.attr.colorSurface.resolveColor(card.context))

		val lastStages = adapter.getLastStages()

		lastStages.forEachIndexed { index, lastStage ->
			val days = TimeHelper.toDays(adapter.plantStages[index][lastStage] ?: 0).toInt()

			if (lastStage.ordinal >= feedingSchedule.stageRange[0].ordinal)
			{
				if (days >= feedingSchedule.dateRange[0]
				&& ((days <= feedingSchedule.dateRange[1] && lastStage.ordinal == feedingSchedule.stageRange[0].ordinal)
					|| (lastStage.ordinal < feedingSchedule.stageRange[1].ordinal)))
				{
					itemView.tag = true
					card.setCardBackgroundColor(android.R.attr.colorAccent.resolveColor(card.context))
					return@forEachIndexed
				}
			}
		}

		title.text = "${feedingSchedule.dateRange[0]}${itemView.context.getString(feedingSchedule.stageRange[0].printString)[0]}"
		if (feedingSchedule.dateRange[0] != feedingSchedule.dateRange[1])
		{
			title.text = "${title.text} - ${feedingSchedule.dateRange[1]}${itemView.context.getString(feedingSchedule.stageRange[1].printString)[0]}"
		}

		var additiveStr = ""
		for (additive in feedingSchedule.additives)
		{
			val converted = Unit.ML.to(measureUnit, additive.amount!!)
			val amountStr = if (converted == floor(converted)) converted.toInt().toString() else converted.toString()

			if (additiveStr.isNotEmpty()) additiveStr += "<br />"
			additiveStr += "• ${additive.description} - ${amountStr}${measureUnit.label}/${deliveryUnit.label}"
		}

		additives.text = Html.fromHtml(additiveStr)

		itemView.setOnClickListener {
			adapter.onItemSelectCallback.invoke(feedingSchedule)
		}
	}
}
