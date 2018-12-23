package me.anon.view

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import kotlinx.android.synthetic.main.feeding_date_stub.view.*
import me.anon.controller.adapter.FeedingDateAdapter
import me.anon.lib.Unit
import me.anon.lib.helper.TimeHelper
import me.anon.model.FeedingScheduleDate

/**
 * // TODO: Add class description
 */
class FeedingDateHolder(val adapter: FeedingDateAdapter, itemView: View) : RecyclerView.ViewHolder(itemView)
{
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
		itemView.setBackgroundColor(0x00FFFFFF.toInt())

		val lastStage = adapter.plantStages.toSortedMap().lastKey()
		val days = TimeHelper.toDays(adapter.plantStages[lastStage] ?: 0).toInt()

		if (lastStage.ordinal >= feedingSchedule.stageRange[0].ordinal)
		{
			if (days >= feedingSchedule.dateRange[0]
			&& ((days <= feedingSchedule.dateRange[1] && lastStage.ordinal == feedingSchedule.stageRange[0].ordinal)
				|| (lastStage.ordinal < feedingSchedule.stageRange[1].ordinal)))
			{
				itemView.setBackgroundColor(0x70BBDEFB.toInt())
			}
		}

		title.text = "${feedingSchedule.dateRange[0]}${feedingSchedule.stageRange[0].printString[0]}"
		if (feedingSchedule.dateRange[0] != feedingSchedule.dateRange[1])
		{
			title.text = "${title.text} - ${feedingSchedule.dateRange[1]}${feedingSchedule.stageRange[1].printString[0]}"
		}

		var additiveStr = ""
		for (additive in feedingSchedule.additives)
		{
			val converted = Unit.ML.to(measureUnit, additive.amount!!)
			val amountStr = if (converted == Math.floor(converted)) converted.toInt().toString() else converted.toString()

			if (additiveStr.isNotEmpty()) additiveStr += "<br />"
			additiveStr += "• ${additive.description} - ${amountStr}${measureUnit.label}/${deliveryUnit.label}"
		}

		additives.text = Html.fromHtml(additiveStr)

		itemView.setOnClickListener {
			adapter.onItemSelectCallback.invoke(feedingSchedule)
		}
	}
}
