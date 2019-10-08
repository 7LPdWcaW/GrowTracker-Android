package me.anon.controller.adapter

import android.content.DialogInterface
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.esotericsoftware.kryo.Kryo
import me.anon.grow.R
import me.anon.lib.TempUnit
import me.anon.lib.ext.formatWhole
import me.anon.lib.ext.getColor
import me.anon.lib.ext.resolveColor
import me.anon.model.Action
import me.anon.model.HumidityChange
import me.anon.model.NoteAction
import me.anon.model.TemperatureChange
import me.anon.view.ActionHolder
import me.anon.view.ImageActionHolder
import java.util.*
import kotlin.collections.ArrayList

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

	public var editListener: (Action) -> Unit = {}
	public var deleteListener: (Action) -> Unit = {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionHolder
	{
		return ActionHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.action_item, parent, false))
	}

	override fun getItemCount(): Int = items.size

	override fun onBindViewHolder(holder: ActionHolder, position: Int)
	{
		val action = items.get(position)
		val tempUnit = TempUnit.getSelectedTemperatureUnit(holder.itemView.context)
		val dateFormat = android.text.format.DateFormat.getDateFormat(holder.itemView.getContext())
		val timeFormat = android.text.format.DateFormat.getTimeFormat(holder.itemView.getContext())

		var dateDay: TextView = holder.getDateDay()

		// plant date & stage
		val actionDate = Date(action.date)
		val actionCalendar = GregorianCalendar.getInstance()
		actionCalendar.time = actionDate

		val fullDateStr = dateFormat.format(actionDate) + " " + timeFormat.format(actionDate)
		val dateDayStr = actionCalendar.get(Calendar.DAY_OF_MONTH).toString() + " " + actionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
		var lastDateStr = ""

		if (position - 1 >= 0)
		{
			val lastActionDate = Date(items.get(position - 1).date)
			val lastActionCalendar = GregorianCalendar.getInstance()
			lastActionCalendar.time = lastActionDate
			lastDateStr = lastActionCalendar.get(Calendar.DAY_OF_MONTH).toString() + " " + lastActionCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
		}

		holder.getCard().setCardBackgroundColor(R.attr.colorSurface.resolveColor(holder.itemView.getContext()))
		holder.getFullDate().setText(Html.fromHtml(fullDateStr))

		var summary = ""
		when (action)
		{
			is TemperatureChange -> {
				holder.name.setText(R.string.temperature_title)
				summary += action.temp.formatWhole() + "°" + tempUnit.label
				holder.getCard().setCardBackgroundColor(-0x654c6225)
			}

			is HumidityChange -> {
				holder.name.setText(R.string.humidity)
				summary += action.humidity?.formatWhole() + "%"
				holder.getCard().setCardBackgroundColor(R.color.light_blue.getColor(holder.itemView.context))
			}

			is NoteAction -> {
				holder.getName().setText(R.string.note)
				holder.getCard().setCardBackgroundColor(0xffe5e5e5.toInt())
			}
		}

		if (action.notes?.isNotEmpty() == true)
		{
			summary += if (summary.length > 0) "<br/><br/>" else ""
			summary += action.notes
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length - "<br/>".length)
		}

		if (!TextUtils.isEmpty(summary))
		{
			holder.getSummary().text = Html.fromHtml(summary)
			holder.getSummary().setVisibility(View.VISIBLE)
		}

		holder.getOverflow().setVisibility(View.VISIBLE)
		holder.getOverflow().setOnClickListener(View.OnClickListener { v ->
			val menu = PopupMenu(v.context, v, Gravity.BOTTOM)
			menu.inflate(R.menu.event_overflow)
			menu.menu.removeItem(R.id.duplicate)
			menu.menu.removeItem(R.id.copy)
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
