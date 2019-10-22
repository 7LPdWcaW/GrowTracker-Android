package me.anon.controller.adapter

import android.text.Html
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow.R
import me.anon.lib.TempUnit
import me.anon.lib.ext.formatWhole
import me.anon.lib.ext.getColor
import me.anon.lib.ext.resolveColor
import me.anon.model.*
import me.anon.view.ActionHolder
import java.util.*

class GardenActionAdapter : RecyclerView.Adapter<ActionHolder>()
{
	public var items: ArrayList<Action> = arrayListOf()
		set(values) {
			field.clear()
			values.asReversed().forEach {
				field.add(it)
			}
			notifyDataSetChanged()
		}

	private var dateFormat: java.text.DateFormat? = null
	private var timeFormat: java.text.DateFormat? = null
	private var tempUnit: TempUnit? = null
	public var editListener: (Action) -> Unit = {}
	public var deleteListener: (Action) -> Unit = {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionHolder
	{
		return ActionHolder(LayoutInflater.from(parent.context).inflate(R.layout.action_item, parent, false))
	}

	override fun getItemCount(): Int = items.size

	override fun onBindViewHolder(holder: ActionHolder, position: Int)
	{
		val action = items[position]
		val tempUnit = tempUnit ?: TempUnit.getSelectedTemperatureUnit(holder.itemView.context).also { tempUnit = it }
		val dateFormat = dateFormat ?: DateFormat.getDateFormat(holder.itemView.context).also { dateFormat = it }
		val timeFormat = timeFormat ?: DateFormat.getTimeFormat(holder.itemView.context).also { timeFormat = it }

		var dateDay: TextView = holder.dateDay

		// plant date & stage
		val actionDate = Date(action.date)
		val actionCalendar = GregorianCalendar.getInstance()
		actionCalendar.time = actionDate

		val fullDateStr = dateFormat.format(actionDate) + " " + timeFormat.format(actionDate)
		val dateDayStr = actionCalendar.get(Calendar.DAY_OF_MONTH).toString() + " " + actionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
		var lastDateStr = ""

		if (position - 1 >= 0)
		{
			val lastActionDate = Date(items[position - 1].date)
			val lastActionCalendar = GregorianCalendar.getInstance()
			lastActionCalendar.time = lastActionDate
			lastDateStr = lastActionCalendar.get(Calendar.DAY_OF_MONTH).toString() + " " + lastActionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
		}

		holder.card.setCardBackgroundColor(R.attr.colorSurface.resolveColor(holder.itemView.context))
		holder.fullDate.text = Html.fromHtml(fullDateStr)

		var summary = ""
		when (action)
		{
			is TemperatureChange -> {
				holder.name.setText(R.string.temperature_title)
				summary += TempUnit.CELCIUS.to(tempUnit, action.temp).formatWhole() + "°" + tempUnit.label
				holder.card.setCardBackgroundColor(-0x654c6225)
			}

			is HumidityChange -> {
				holder.name.setText(R.string.humidity)
				summary += action.humidity?.formatWhole() + "%"
				holder.card.setCardBackgroundColor(R.color.light_blue.getColor(holder.itemView.context))
			}

			is NoteAction -> {
				holder.name.setText(R.string.note)
				holder.card.setCardBackgroundColor(0xffe5e5e5.toInt())
			}

			is LightingChange -> {
				holder.name.setText(R.string.lighting_title)
				holder.card.setCardBackgroundColor(R.color.light_yellow.getColor(holder.itemView.context))
				summary += "<b>" + holder.itemView.context.getString(R.string.lights_on) + ":</b> "
				summary += action.on + "<br/>"
				summary += "<b>" + holder.itemView.context.getString(R.string.lights_off) + ":</b> "
				summary += action.off
			}
		}

		if (action.notes?.isNotEmpty() == true)
		{
			summary += if (summary.isNotEmpty()) "<br/><br/>" else ""
			summary += action.notes
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length - "<br/>".length)
		}

		if (!TextUtils.isEmpty(summary))
		{
			holder.summary.text = Html.fromHtml(summary)
			holder.summary.visibility = View.VISIBLE
		}

		holder.overflow.visibility = View.VISIBLE
		holder.overflow.setOnClickListener(View.OnClickListener { v ->
			val menu = PopupMenu(v.context, v, Gravity.BOTTOM)
			menu.inflate(R.menu.event_overflow)
			menu.menu.removeItem(R.id.duplicate)
			menu.menu.removeItem(R.id.copy)

			if (action is LightingChange)
			{
				menu.menu.removeItem(R.id.edit)
			}

			menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
				when (item.itemId)
				{
					R.id.edit -> {
						editListener(action)
						return@OnMenuItemClickListener true
					}

					R.id.delete -> {
						deleteListener(action)
						return@OnMenuItemClickListener true
					}
				}

				false
			})

			menu.show()
		})

		if (!lastDateStr.equals(dateDayStr, ignoreCase = true))
		{
			dateDay.text = Html.fromHtml(dateDayStr)
			dateDay.visibility = View.VISIBLE
		}
		else
		{
			dateDay.visibility = View.GONE
		}
	}
}
