package me.anon.grow.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.anon.grow.R
import me.anon.lib.TdsUnit
import me.anon.lib.TdsUnit.Companion.getSelectedTdsUnit
import me.anon.lib.Unit
import me.anon.lib.ext.inflate
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
class StatisticsFragment2 : Fragment()
{
	companion object
	{
		@JvmStatic
		public fun newInstance(args: Bundle) = StatisticsFragment().apply {
			this.arguments = args
		}
	}

	private lateinit var plant: Plant
	private val selectedTdsUnit by lazy { TdsUnit.getSelectedTdsUnit(activity!!) }
	private val selectedDeliveryUnit by lazy { Unit.getSelectedDeliveryUnit(activity!!) }
	private val selectedMeasurementUnit by lazy { Unit.getSelectedMeasurementUnit(activity!!) }
	private val checkedAdditives = setOf<String>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.statistics_view, container, false)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
	}
}
