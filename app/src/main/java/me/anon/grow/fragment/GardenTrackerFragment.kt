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
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.esotericsoftware.kryo.Kryo
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.data_label_stub.view.*
import kotlinx.android.synthetic.main.garden_tracker_view.*
import me.anon.controller.adapter.GardenActionAdapter
import me.anon.grow.MainActivity
import me.anon.grow.R
import me.anon.lib.TempUnit
import me.anon.lib.ext.*
import me.anon.lib.manager.GardenManager
import me.anon.model.*
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.garden_tracker_view, container, false)
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
			dialogFragment.setOnDialogConfirmed {
				garden.actions.add(NoteAction(notes = it))
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
			lighton_input.text = it.on
			lightoff_input.text = it.off

			if (it.off.isEmpty()) return@let
			val lightOnTime = LocalTime.parse(it.on, DateTimeFormatter.ofPattern("HH:mm"))
			val lightOffTime = LocalTime.parse(it.off, DateTimeFormatter.ofPattern("HH:mm"))
			var diff = (abs(Duration.between(lightOnTime, lightOffTime).toMinutes()) / 15.0)
			if (diff == 0.0) diff = 96.0
			light_ratio.progress = diff.toInt()

			val on = ((light_ratio.progress.toDouble() * 15.0) / 60.0).round(2).formatWhole()
			val off = ((1440.0 - (light_ratio.progress.toDouble() * 15.0)) / 60.0).round(2).formatWhole()
			progresson_label.text = "$on" + getString(R.string.hour_abbr)
			progressoff_label.text = "$off" + getString(R.string.hour_abbr)
		}

		lightson_container.setOnClickListener {
			val currentLighting = (transactions.lastOrNull { it is LightingChange } ?: Kryo().copy(garden.actions.lastOrNull { it is LightingChange })) as LightingChange? ?: LightingChange()

			val lightOnTime = LocalTime.parse(currentLighting.on, DateTimeFormatter.ofPattern("HH:mm"))
			val dialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
				light_ratio.isEnabled = true
				val currentProgress = light_ratio.progress
				val timeOff = LocalTime.of(hourOfDay, minute).plusMinutes(currentProgress.toLong() * 15)

				lighton_input.text = LocalTime.of(hourOfDay, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
				lightoff_input.text = timeOff.format(DateTimeFormatter.ofPattern("HH:mm"))

				currentLighting.on = lighton_input.text.toString()
				currentLighting.off = lightoff_input.text.toString()
				currentLighting.date = System.currentTimeMillis()

				addTransaction(currentLighting)
			}, lightOnTime.hour, lightOnTime.minute, true)
			dialog.show()
		}

		lightsoff_container.setOnClickListener {
			val currentLighting = (transactions.lastOrNull { it is LightingChange } ?: Kryo().copy(garden.actions.lastOrNull { it is LightingChange })) as LightingChange? ?: LightingChange()

			val lightOffTime = LocalTime.parse(currentLighting.off, DateTimeFormatter.ofPattern("HH:mm"))
			val dialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
				light_ratio.isEnabled = true
				val currentProgress = light_ratio.progress
				val timeOn = LocalTime.of(hourOfDay, minute).minusMinutes(currentProgress.toLong() * 15)

				lightoff_input.text = LocalTime.of(hourOfDay, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
				lighton_input.text = timeOn.format(DateTimeFormatter.ofPattern("HH:mm"))

				currentLighting.on = lighton_input.text.toString()
				currentLighting.off = lightoff_input.text.toString()
				currentLighting.date = System.currentTimeMillis()

				addTransaction(currentLighting)
			}, lightOffTime.hour, lightOffTime.minute, true)
			dialog.show()
		}

		light_ratio.isEnabled = currentLighting != null
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
			override fun onStopTrackingTouch(p0: SeekBar?)
			{
				val currentLighting = (transactions.lastOrNull { it is LightingChange } ?: garden.actions.lastOrNull { it is LightingChange }) as LightingChange?
				currentLighting?.let {
					currentLighting.on = lighton_input.text.toString()
					currentLighting.off = lightoff_input.text.toString()
				}
			}
		})

		fake_toggle_actions.setOnClickListener {
			actions_container.visibility = View.VISIBLE
			details_container.visibility = View.GONE
		}

		toggle_actions.setOnClickListener {
			actions_container.visibility = View.GONE
			details_container.visibility = View.VISIBLE
		}

		actions_recycler.adapter ?: let {
			actions_recycler.adapter = GardenActionAdapter()
			actions_recycler.layoutManager = LinearLayoutManager(activity!!)
		}

		updateDataReferences()

		(actions_recycler.adapter as GardenActionAdapter?)?.let {
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
				dialogFragment.setOnDialogConfirmed { notes ->
					if (index > -1) garden.actions[index].notes = notes
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

		AlertDialog.Builder(activity!!)
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
		val tempUnit = TempUnit.getSelectedTemperatureUnit(activity!!)

		val tempValues = ArrayList<Entry>()
		val tempStats = StatisticsFragment2.StatisticsViewModel.StatWrapper()
		val tempTempValues = arrayListOf<Double>()
		val humidityValues = ArrayList<Entry>()
		val humidityStats = StatisticsFragment2.StatisticsViewModel.StatWrapper()
		val humidityTempValues = arrayListOf<Double>()
		garden.actions.forEach { action ->
			when (action)
			{
				is TemperatureChange -> {
					action.temp.let {
						tempTempValues += it
						tempStats.max = max(tempStats.max ?: Double.MIN_VALUE, it)
						tempStats.min = min(tempStats.min ?: Double.MAX_VALUE, it)

						tempValues += Entry(tempValues.size.toFloat(), it.toFloat())
					}
				}

				is HumidityChange -> {
					action.humidity?.let {
						humidityTempValues += it
						humidityStats.max = max(humidityStats.max ?: Double.MIN_VALUE, it)
						humidityStats.min = min(humidityStats.min ?: Double.MAX_VALUE, it)

						humidityValues += Entry(humidityValues.size.toFloat(), it.toFloat())
					}
				}
			}
		}
		tempStats.average = if (tempTempValues.isNotEmpty()) tempTempValues.average() else null
		humidityStats.average = if (humidityTempValues.isNotEmpty()) humidityTempValues.average() else null

		(garden.actions.lastOrNull { it is TemperatureChange } as TemperatureChange?)?.let {
			val view = data_container.findViewById<View?>(R.id.stats_temp) ?: data_container.inflate(R.layout.data_label_stub)
			view.label.setText(R.string.current_temp)
			view.data.text = "${TempUnit.CELCIUS.to(tempUnit, it.temp).formatWhole()}°${tempUnit.label}"
			view.id = R.id.stats_temp

			if (data_container.findViewById<View?>(R.id.stats_temp) == null) data_container.addView(view)
		}


		min_temp.text = "${tempStats.min ?: "-"}°${tempUnit.label}"
		max_temp.text = "${tempStats.max ?: "-"}°${tempUnit.label}"
		ave_temp.text = "${tempStats.average ?: "-"}°${tempUnit.label}"

		val humidityAdditional = arrayOfNulls<String>(3)
		(garden.actions.lastOrNull { it is HumidityChange } as HumidityChange?)?.let {
			val view = data_container.findViewById<View?>(R.id.stats_humidity) ?: data_container.inflate(R.layout.data_label_stub)
			view.label.setText(R.string.current_humidity)
			view.data.text = "${it.humidity?.formatWhole() ?: 0}%"
			view.id = R.id.stats_humidity

			if (data_container.findViewById<View?>(R.id.stats_humidity) == null) data_container.addView(view)
		}

		min_humidity.text = "${humidityStats.min ?: "-"}%"
		max_humidity.text = "${humidityStats.max ?: "-"}%"
		ave_humidity.text = "${humidityStats.average ?: "-"}%"

		with (temp) {
			setVisibleYRangeMaximum(tempStats.max?.toFloat() ?: 0.0f, YAxis.AxisDependency.LEFT)
			style()

			axisLeft.valueFormatter = object : ValueFormatter()
			{
				override fun getAxisLabel(value: Float, axis: AxisBase?): String
				{
					return "${value.formatWhole()}°${tempUnit.label}"
				}
			}

			marker = object : MarkerView(activity, R.layout.chart_marker)
			{
				override fun refreshContent(e: Entry, highlight: Highlight): kotlin.Unit
				{
					val color = temp.data.dataSets[highlight.dataSetIndex].color
					with (this.findViewById<TextView>(R.id.content)) {
						text = "${e.y.formatWhole()}°${tempUnit.label}"
						setTextColor(color)
					}

					super.refreshContent(e, highlight)
				}

				override fun getOffset(): MPPointF = MPPointF.getInstance(-(width / 2f), -(height * 1.2f))
			}

			//xAxis.valueFormatter = datesFormatter
		}

		val sets = arrayListOf<ILineDataSet>()

		sets += LineDataSet(tempValues, getString(R.string.stat_input_ph)).apply {
			color = 0xffe6194B.toInt()
			fillColor = color
			setCircleColor(color)
			StatisticsFragment2.styleDataset(context!!, this, color)
		}

		sets += LineDataSet(tempValues.rollingAverage(), getString(R.string.stat_average_temp)).apply {
			color = ColorUtils.blendARGB(0xffe6194B.toInt(), 0xffffffff.toInt(), 0.4f)
			setDrawCircles(false)
			setDrawValues(false)
			setDrawCircleHole(false)
			setDrawHighlightIndicators(true)
			cubicIntensity = 1f
			lineWidth = 2.0f
			isHighlightEnabled = false
		}

		temp.data = LineData(sets)

//		humidity.setOnChartValueSelectedListener(object : OnChartValueSelectedListener
//		{
//			override fun onNothingSelected()
//			{
//				edit_humidity.visibility = View.GONE
//				delete_humidity.visibility = View.GONE
//			}
//
//			override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?)
//			{
//				edit_humidity.visibility = View.VISIBLE
//				delete_humidity.visibility = View.VISIBLE
//
//				edit_humidity.setOnClickListener {
//					(e?.data as HumidityChange?)?.let { current ->
//						editAction(current)
//					}
//				}
//
//				delete_humidity.setOnClickListener {
//					(e?.data as Action?)?.let {
//						deleteAction(it)
//					}
//				}
//			}
//		})
//
//		temp.setOnChartValueSelectedListener(object : OnChartValueSelectedListener
//		{
//			override fun onNothingSelected()
//			{
//				edit_temp.visibility = View.GONE
//				delete_temp.visibility = View.GONE
//			}
//
//			override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?)
//			{
//				edit_temp.visibility = View.VISIBLE
//				delete_temp.visibility = View.VISIBLE
//
//				edit_temp.setOnClickListener {
//					(e?.data as TemperatureChange?)?.let { current ->
//						editAction(current)
//					}
//				}
//
//				delete_temp.setOnClickListener {
//					(e?.data as Action?)?.let { current ->
//						deleteAction(current)
//					}
//				}
//			}
//		})

		general_title.visibility = if (data_container.childCount > 0) View.VISIBLE else View.GONE
		data_container.visibility = if (data_container.childCount > 0) View.VISIBLE else View.GONE
	}

	private fun updateDataReferences()
	{
		(actions_recycler.adapter as GardenActionAdapter?)?.let {
			it.items = garden.actions

			if (it.items.size > 0)
			{
				actions_recycler.visibility = View.VISIBLE
				empty.visibility = View.GONE
			}
			else
			{
				actions_recycler.visibility = View.GONE
				empty.visibility = View.VISIBLE
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
