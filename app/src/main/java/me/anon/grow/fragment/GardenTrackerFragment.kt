package me.anon.grow.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.data_label_stub.view.*
import kotlinx.android.synthetic.main.garden_tracker_view.*
import me.anon.grow.MainActivity
import me.anon.grow.R
import me.anon.lib.TempUnit
import me.anon.lib.ext.formatWhole
import me.anon.lib.ext.inflate
import me.anon.lib.ext.round
import me.anon.lib.helper.StatsHelper
import me.anon.lib.manager.GardenManager
import me.anon.model.Garden
import me.anon.model.LightingChange
import me.anon.model.TemperatureChange
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

class GardenTrackerFragment : Fragment()
{
	protected lateinit var garden: Garden

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.garden_tracker_view, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		(activity as MainActivity).toolbarLayout.removeViews(1, (activity as MainActivity).toolbarLayout.childCount - 1)
		(activity as MainActivity).toolbarLayout.addView(LayoutInflater.from(activity).inflate(R.layout.garden_action_buttons_stub, (activity as MainActivity).toolbarLayout, false))

		(activity as MainActivity).toolbarLayout.findViewById<View>(R.id.temp).setOnClickListener {
			val dialogFragment = TemperatureDialogFragment() {
				garden.actions.add(it)
				updateDataReferences()
			}
			dialogFragment.show(childFragmentManager, null)
		}

		setUi()
	}

	private fun setUi()
	{
		lightson_container.setOnClickListener {
			val currentLighting = garden.actions.lastOrNull { it is LightingChange } as LightingChange? ?: LightingChange()
			val lightOnTime = LocalTime.parse(currentLighting.on, DateTimeFormatter.ofPattern("HH:mm"))
			val dialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
				val currentProgress = light_ratio.progress
				val timeOff = LocalTime.of(hourOfDay, minute).plusMinutes(currentProgress.toLong() * 15)

				lighton_input.text = LocalTime.of(hourOfDay, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
				lightoff_input.text = timeOff.format(DateTimeFormatter.ofPattern("HH:mm"))
			}, lightOnTime.hour, lightOnTime.minute, true)
			dialog.show()
		}

		light_ratio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
		{
			override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean)
			{
				val on = ((progress.toDouble() * 15.0) / 60.0).round(2).formatWhole()
				val off = ((1440.0 - (progress.toDouble() * 15.0)) / 60.0).round(2).formatWhole()
				progresson_label.text = "$on" + getString(R.string.hour_abbr)
				progressoff_label.text = "$off" + getString(R.string.hour_abbr)

				if (lighton_input.text.isNotEmpty())
				{
					val timeOff = LocalTime.parse(lighton_input.text, DateTimeFormatter.ofPattern("HH:mm")).plusMinutes(progress.toLong() * 15)
					lightoff_input.text = timeOff.format(DateTimeFormatter.ofPattern("HH:mm"))
				}
			}

			override fun onStartTrackingTouch(p0: SeekBar?){}
			override fun onStopTrackingTouch(p0: SeekBar?){}
		})

		setStatistics()
	}

	private fun setStatistics()
	{
		val tempUnit = TempUnit.getSelectedTemperatureUnit(activity!!)
		val tempAdditional = arrayOfNulls<String>(3)
		(garden.actions.lastOrNull { it is TemperatureChange } as TemperatureChange?)?.let {
			val view = data_container.findViewById<View?>(R.id.stats_temp) ?: data_container.inflate(R.layout.data_label_stub)
			view.label.setText(R.string.current_temp)
			view.data.text = "${TempUnit.CELCIUS.to(tempUnit, it.temp)}°${tempUnit.label}"
			view.id = R.id.stats_temp

			if (data_container.findViewById<View?>(R.id.stats_temp) == null) data_container.addView(view)
		}
		StatsHelper.setTempData(garden, activity!!, temp, tempAdditional)
		min_temp.text = if (tempAdditional[0] == "100") "-" else "${tempAdditional[0]}°${tempUnit.label}"
		max_temp.text = if (tempAdditional[1] == "-100") "-" else "${tempAdditional[0]}°${tempUnit.label}"
		ave_temp.text = "${tempAdditional[2]}°${tempUnit.label}"
	}

	private fun updateDataReferences()
	{
		setStatistics()

		if (parentFragment is GardenHostFragment)
		{
			GardenManager.getInstance().upsert(garden)
			(parentFragment as GardenHostFragment).garden = garden
		}
	}

	companion object
	{
		@JvmStatic
		fun newInstance(garden: Garden): GardenTrackerFragment
		{
			val fragment = GardenTrackerFragment()
			fragment.garden = garden

			return fragment
		}
	}
}
