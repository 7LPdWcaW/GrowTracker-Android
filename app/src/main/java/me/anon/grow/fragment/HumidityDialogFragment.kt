package me.anon.grow.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import me.anon.grow.R
import me.anon.lib.ext.formatWhole
import me.anon.model.HumidityChange
import java.util.*

class HumidityDialogFragment(var action: HumidityChange? = null, val callback: (action: HumidityChange) -> Unit = {}) : DialogFragment()
{
	private val newAction = action == null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		val view = LayoutInflater.from(activity).inflate(R.layout.humidity_dialog, null)
		val dialog = AlertDialog.Builder(context!!)
		dialog.setTitle(R.string.humidity)
		dialog.setPositiveButton(if (newAction) R.string.add else R.string.edit) { dialog, which ->
			if (view.findViewById<EditText>(R.id.humidity_input).text.toString().isNotBlank())
			{
				action?.let {
					it.humidity = view.findViewById<EditText>(R.id.humidity_input).text.toString().toDouble()

					if (view.findViewById<EditText>(R.id.notes).text.isNotEmpty())
					{
						it.notes = view.findViewById<EditText>(R.id.notes).text.toString()
					}

					callback(it)
				}
			}
		}

		if (action == null)
		{
			action = HumidityChange()
		}

		view.findViewById<EditText>(R.id.notes).setText(action?.notes)
		view.findViewById<EditText>(R.id.humidity_input).hint = "32%"
		if (!newAction)
		{
			view.findViewById<EditText>(R.id.humidity_input).setText("${action!!.humidity?.formatWhole()}")
		}

		val date = Calendar.getInstance()
		date.timeInMillis = action!!.date

		val dateFormat = android.text.format.DateFormat.getDateFormat(activity)
		val timeFormat = android.text.format.DateFormat.getTimeFormat(activity)

		val dateStr = dateFormat.format(Date(action!!.date)) + " " + timeFormat.format(Date(action!!.date))

		view.findViewById<TextView>(R.id.date).text = dateStr
		view.findViewById<TextView>(R.id.date).setOnClickListener {
			val fragment = DateDialogFragment(action!!.date)
			fragment.setOnDateSelected(object : DateDialogFragment.OnDateSelectedListener
			{
				override fun onDateSelected(date: Calendar)
				{
					val dateStr = dateFormat.format(date.time) + " " + timeFormat.format(date.time)
					view.findViewById<TextView>(R.id.date).text = dateStr

					action!!.date = date.timeInMillis
					onCancelled()
				}

				override fun onCancelled()
				{
					childFragmentManager.beginTransaction().remove(fragment).commit()
				}
			})
			childFragmentManager.beginTransaction().add(fragment, "date").commit()
		}

		dialog.setView(view)
		dialog.setNegativeButton(R.string.cancel) { dialogInterface, i -> onCancel(dialogInterface) }

		return dialog.create()
	}
}
