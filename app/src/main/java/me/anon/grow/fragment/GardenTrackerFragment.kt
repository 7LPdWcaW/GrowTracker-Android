package me.anon.grow.fragment

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.esotericsoftware.kryo.Kryo
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import me.anon.controller.adapter.GardenActionAdapter
import me.anon.grow.MainActivity
import me.anon.grow.R
import me.anon.grow.databinding.GardenTrackerViewBinding
import me.anon.lib.TempUnit
import me.anon.lib.ext.formatWhole
import me.anon.lib.ext.inflate
import me.anon.lib.ext.round
import me.anon.lib.helper.StatsHelper
import me.anon.lib.manager.GardenManager
import me.anon.model.*
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class GardenTrackerFragment : Fragment()
{
	protected lateinit var garden: Garden
	private val transactions: ArrayList<Action> = arrayListOf()

	private fun addTransaction(action: Action)
	{
		transactions.find { it.javaClass == action.javaClass }?.let {
			transactions.remove(it)
		}

		val lastLightingChange = (garden.actions.findLast { it is LightingChange }) as LightingChange?
		if (lastLightingChange != null && action is LightingChange)
		{
			if (lastLightingChange.on == action.on && lastLightingChange.off == action.off) return
		}

		transactions.add(action)
	}

	private fun commitTransaction()
	{
		transactions.forEach { action ->
			garden.actions.add(action)
		}

		transactions.clear()
		save()
	}


	private lateinit var binding: GardenTrackerViewBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= GardenTrackerViewBinding.inflate(inflater).let {
		binding = it
		it.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		savedInstanceState?.let {
			garden = it.getParcelable("garden")!!
		}

		(activity as MainActivity).toolbarLayout.removeViews(1, (activity as MainActivity).toolbarLayout.childCount - 1)
		(activity as MainActivity).toolbarLayout.addView(LayoutInflater.from(activity).inflate(R.layout.garden_action_buttons_stub, (activity as MainActivity).toolbarLayout, false))

		(activity as MainActivity).toolbarLayout.findViewById<View>(R.id.temp).setOnClickListener {
			val dialogFragment = TemperatureDialogFragment {
				garden.actions.add(it)
				updateDataReferences()
			}
			dialogFragment.show(childFragmentManager, null)
		}

		(activity as MainActivity).toolbarLayout.findViewById<View>(R.id.humidity).setOnClickListener {
			val dialogFragment = HumidityDialogFragment {
				garden.actions.add(it)
				updateDataReferences()
			}
			dialogFragment.show(childFragmentManager, null)
		}

		(activity as MainActivity).toolbarLayout.findViewById<View>(R.id.note).setOnClickListener {
			val dialogFragment = NoteDialogFragment()
			dialogFragment.setOnDialogConfirmed { notes, date ->
				garden.actions.add(NoteAction(notes = notes, date = date.time))
				updateDataReferences()
			}
			dialogFragment.show(childFragmentManager, null)
		}

		setUi()
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		outState.putParcelable("garden", garden)
		super.onSaveInstanceState(outState)
	}

	override fun onDestroy()
	{
		commitTransaction()
		super.onDestroy()
	}

	private fun setUi()
	{
		var currentLighting = (transactions.lastOrNull { it is LightingChange } ?: garden.actions.lastOrNull { it is LightingChange }) as LightingChange?
		currentLighting?.let {
			binding.lightonInput.text = it.on
			binding.lightoffInput.text = it.off

			if (it.off.isEmpty()) return@let
			val lightOnTime = LocalTime.parse(it.on, DateTimeFormatter.ofPattern("HH:mm"))
			val lightOffTime = LocalTime.parse(it.off, DateTimeFormatter.ofPattern("HH:mm"))
			var diff = (abs(Duration.between(lightOnTime, lightOffTime).toMinutes()) / 15.0)
			if (diff == 0.0) diff = 96.0
			binding.lightRatio.progress = diff.toInt()

			val on = ((binding.lightRatio.progress.toDouble() * 15.0) / 60.0).round(2).formatWhole()
			val off = ((1440.0 - (binding.lightRatio.progress.toDouble() * 15.0)) / 60.0).round(2).formatWhole()
			binding.progressonLabel.text = "$on" + getString(R.string.hour_abbr)
			binding.progressoffLabel.text = "$off" + getString(R.string.hour_abbr)
		}

		binding.lightsonContainer.setOnClickListener {
			val currentLighting = (transactions.lastOrNull { it is LightingChange } ?: Kryo().copy(garden.actions.lastOrNull { it is LightingChange })) as LightingChange? ?: LightingChange()

			val lightOnTime = LocalTime.parse(currentLighting.on, DateTimeFormatter.ofPattern("HH:mm"))
			val dialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
				binding.lightRatio.isEnabled = true
				val currentProgress = binding.lightRatio.progress
				val timeOff = LocalTime.of(hourOfDay, minute).plusMinutes(currentProgress.toLong() * 15)

				binding.lightonInput.text = LocalTime.of(hourOfDay, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
				binding.lightoffInput.text = timeOff.format(DateTimeFormatter.ofPattern("HH:mm"))

				currentLighting.on = binding.lightonInput.text.toString()
				currentLighting.off = binding.lightoffInput.text.toString()
				currentLighting.date = System.currentTimeMillis()

				addTransaction(currentLighting)
			}, lightOnTime.hour, lightOnTime.minute, true)
			dialog.show()
		}

		binding.lightsoffContainer.setOnClickListener {
			val currentLighting = (transactions.lastOrNull { it is LightingChange } ?: Kryo().copy(garden.actions.lastOrNull { it is LightingChange })) as LightingChange? ?: LightingChange()

			val lightOffTime = LocalTime.parse(currentLighting.off, DateTimeFormatter.ofPattern("HH:mm"))
			val dialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
				binding.lightRatio.isEnabled = true
				val currentProgress = binding.lightRatio.progress
				val timeOn = LocalTime.of(hourOfDay, minute).minusMinutes(currentProgress.toLong() * 15)

				binding.lightoffInput.text = LocalTime.of(hourOfDay, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
				binding.lightonInput.text = timeOn.format(DateTimeFormatter.ofPattern("HH:mm"))

				currentLighting.on = binding.lightonInput.text.toString()
				currentLighting.off = binding.lightoffInput.text.toString()
				currentLighting.date = System.currentTimeMillis()

				addTransaction(currentLighting)
			}, lightOffTime.hour, lightOffTime.minute, true)
			dialog.show()
		}

		binding.lightRatio.isEnabled = currentLighting != null
		binding.lightRatio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
		{
			override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean)
			{
				val on = ((progress.toDouble() * 15.0) / 60.0).round(2).formatWhole()
				val off = ((1440.0 - (progress.toDouble() * 15.0)) / 60.0).round(2).formatWhole()
				binding.progressonLabel.text = "$on" + getString(R.string.hour_abbr)
				binding.progressoffLabel.text = "$off" + getString(R.string.hour_abbr)

				if (binding.lightonInput.text.isNotEmpty())
				{
					val timeOff = LocalTime.parse(binding.lightonInput.text, DateTimeFormatter.ofPattern("HH:mm")).plusMinutes(progress.toLong() * 15)
					binding.lightoffInput.text = timeOff.format(DateTimeFormatter.ofPattern("HH:mm"))
				}
			}

			override fun onStartTrackingTouch(p0: SeekBar?){}
			override fun onStopTrackingTouch(p0: SeekBar?)
			{
				val currentLighting = (transactions.lastOrNull { it is LightingChange } ?: garden.actions.lastOrNull { it is LightingChange }) as LightingChange?
				currentLighting?.let {
					currentLighting.on = binding.lightonInput.text.toString()
					currentLighting.off = binding.lightoffInput.text.toString()
				}
			}
		})

		binding.fakeToggleActions.setOnClickListener {
			binding.actionsContainer.visibility = View.VISIBLE
			binding.detailsContainer.visibility = View.GONE
		}

		binding.toggleActions.setOnClickListener {
			binding.actionsContainer.visibility = View.GONE
			binding.detailsContainer.visibility = View.VISIBLE
		}

		binding.actionsRecycler.adapter ?: let {
			binding.actionsRecycler.adapter = GardenActionAdapter()
			binding.actionsRecycler.layoutManager = LinearLayoutManager(requireActivity())
		}

		updateDataReferences()

		(binding.actionsRecycler.adapter as GardenActionAdapter?)?.let {
			it.editListener = ::editAction
			it.deleteListener = ::deleteAction
		}
	}

	private fun editAction(action: Action)
	{
		val index = garden.actions.indexOfLast { it.date == action.date }
		when (action)
		{
			is HumidityChange -> {
				val dialogFragment = HumidityDialogFragment(action) {
					if (index > -1) garden.actions[index] = action
					updateDataReferences()
				}
				dialogFragment.show(childFragmentManager, null)
			}

			is TemperatureChange -> {
				val dialogFragment = TemperatureDialogFragment(action) { action ->
					if (index > -1) garden.actions[index] = action
					updateDataReferences()
				}
				dialogFragment.show(childFragmentManager, null)
			}

			is NoteAction -> {
				val dialogFragment = NoteDialogFragment(action)
				dialogFragment.setOnDialogConfirmed { notes, dates ->
					if (index > -1)
					{
						garden.actions[index].notes = notes
						garden.actions[index].date = dates.time
					}

					updateDataReferences()
				}
				dialogFragment.show(childFragmentManager, null)
			}
		}

		updateDataReferences()
	}

	private fun deleteAction(action: Action)
	{
		val title = when (action)
		{
			is HumidityChange -> R.string.humidity
			is TemperatureChange -> R.string.temperature_title
			is NoteAction -> R.string.note
			is LightingChange -> R.string.lighting_title
			else -> -1
		}

		AlertDialog.Builder(requireActivity())
			.setTitle(R.string.delete_item_dialog_title)
			.setMessage(Html.fromHtml(getString(R.string.confirm_delete_item_message_holder, getString(title))))
			.setPositiveButton(R.string.delete, DialogInterface.OnClickListener { dialogInterface, i ->
				garden.actions.removeAll { it.date == action.date }
				updateDataReferences()
			})
			.setNegativeButton(R.string.cancel, null)
			.show()
	}

	private fun setStatistics()
	{
		val tempUnit = TempUnit.getSelectedTemperatureUnit(requireActivity())
		val tempAdditional = arrayOfNulls<String>(3)
		(garden.actions.lastOrNull { it is TemperatureChange } as TemperatureChange?)?.let {
			val view = binding.dataContainer.findViewById<View?>(R.id.stats_temp) ?: binding.dataContainer.inflate(R.layout.data_label_stub)
			view.findViewById<TextView>(R.id.label).setText(R.string.current_temp)
			view.findViewById<TextView>(R.id.data).text = "${TempUnit.CELCIUS.to(tempUnit, it.temp).formatWhole()}°${tempUnit.label}"
			view.id = R.id.stats_temp

			if (binding.dataContainer.findViewById<View?>(R.id.stats_temp) == null) binding.dataContainer.addView(view)
		}
		StatsHelper.setTempData(garden, requireActivity(), binding.temp, tempAdditional)

		binding.temp.marker = object : MarkerView(context, R.layout.chart_marker)
		{
			override fun refreshContent(e: Entry, highlight: Highlight)
			{
				val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
				val action = e.data as Action
				var date = ""

				if (action != null) date = "\n" + timeFormat.format(Date(action.date))

				(findViewById<View>(R.id.content) as TextView).text = e.y.formatWhole() + "°" + tempUnit.label + date
				super.refreshContent(e, highlight)
			}

			override fun getOffset(): MPPointF = MPPointF.getInstance(-(width / 2f), -(height * 1.2f))
		}
		binding.temp.notifyDataSetChanged()
		binding.temp.postInvalidate()
		binding.minTemp.text = if (tempAdditional[0] == "100") "-" else "${tempAdditional[0]}°${tempUnit.label}"
		binding.maxTemp.text = if (tempAdditional[1] == "-100") "-" else "${tempAdditional[1]}°${tempUnit.label}"
		binding.aveTemp.text = "${tempAdditional[2]}°${tempUnit.label}"

		val humidityAdditional = arrayOfNulls<String>(3)
		(garden.actions.lastOrNull { it is HumidityChange } as HumidityChange?)?.let {
			val view = binding.dataContainer.findViewById<View?>(R.id.stats_humidity) ?: binding.dataContainer.inflate(R.layout.data_label_stub)
			view.findViewById<TextView>(R.id.label).setText(R.string.current_humidity)
			view.findViewById<TextView>(R.id.data).text = "${it.humidity?.formatWhole() ?: 0}%"
			view.id = R.id.stats_humidity

			if (binding.dataContainer.findViewById<View?>(R.id.stats_humidity) == null) binding.dataContainer.addView(view)
		}
		StatsHelper.setHumidityData(garden, requireActivity(), binding.humidity, humidityAdditional)
		binding.humidity.marker = object : MarkerView(context, R.layout.chart_marker)
		{
			override fun refreshContent(e: Entry, highlight: Highlight)
			{
				val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
				val action = e.data as Action
				var date = ""

				if (action != null) date = "\n" + timeFormat.format(Date(action.date))
				(findViewById<View>(R.id.content) as TextView).text = e.y.toInt().toString() + "%" + date
				super.refreshContent(e, highlight)
			}

			override fun getOffset(): MPPointF = MPPointF.getInstance(-(width / 2f), -(height * 1.2f))
		}
		binding.humidity.notifyDataSetChanged()
		binding.humidity.postInvalidate()
		binding.minHumidity.text = if (humidityAdditional[0] == "100") "-" else "${humidityAdditional[0]}%"
		binding.maxHumidity.text = if (humidityAdditional[1] == "-100") "-" else "${humidityAdditional[1]}%"
		binding.aveHumidity.text = "${humidityAdditional[2]}%"

		binding.humidity.setOnChartValueSelectedListener(object : OnChartValueSelectedListener
		{
			override fun onNothingSelected()
			{
				binding.editHumidity.visibility = View.GONE
				binding.deleteHumidity.visibility = View.GONE
			}

			override fun onValueSelected(e: Entry?, h: Highlight?)
			{
				binding.editHumidity.visibility = View.VISIBLE
				binding.deleteHumidity.visibility = View.VISIBLE

				binding.editHumidity.setOnClickListener {
					(e?.data as HumidityChange?)?.let { current ->
						editAction(current)
					}
				}

				binding.deleteHumidity.setOnClickListener {
					(e?.data as Action?)?.let {
						deleteAction(it)
					}
				}
			}
		})

		binding.temp.setOnChartValueSelectedListener(object : OnChartValueSelectedListener
		{
			override fun onNothingSelected()
			{
				binding.editTemp.visibility = View.GONE
				binding.deleteTemp.visibility = View.GONE
			}

			override fun onValueSelected(e: Entry?, h: Highlight?)
			{
				binding.editTemp.visibility = View.VISIBLE
				binding.deleteTemp.visibility = View.VISIBLE

				binding.editTemp.setOnClickListener {
					(e?.data as TemperatureChange?)?.let { current ->
						editAction(current)
					}
				}

				binding.deleteTemp.setOnClickListener {
					(e?.data as Action?)?.let { current ->
						deleteAction(current)
					}
				}
			}
		})

		binding.generalTitle.visibility = if (binding.dataContainer.childCount > 0) View.VISIBLE else View.GONE
		binding.dataContainer.visibility = if (binding.dataContainer.childCount > 0) View.VISIBLE else View.GONE
	}

	private fun updateDataReferences()
	{
		(binding.actionsRecycler.adapter as GardenActionAdapter?)?.let {
			it.items = garden.actions

			if (it.items.size > 0)
			{
				binding.actionsRecycler.visibility = View.VISIBLE
				binding.empty.visibility = View.GONE
			}
			else
			{
				binding.actionsRecycler.visibility = View.GONE
				binding.empty.visibility = View.VISIBLE
			}
		}

		setStatistics()
		save()
	}

	private fun save()
	{
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
