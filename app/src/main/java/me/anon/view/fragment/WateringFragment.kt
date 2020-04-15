package me.anon.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.add_watering_view.*
import me.anon.grow.R
import me.anon.lib.ext.formatWhole
import me.anon.lib.ext.toDoubleOrNull
import me.anon.lib.ext.viewModelFactory
import me.anon.view.viewmodel.WateringViewModel

/**
 * // TODO: Add class description
 */
class WateringFragment : Fragment(R.layout.add_watering_view)
{
	companion object
	{
		public fun newInstance(plantId: String, actionId: String? = null) = WateringFragment().apply {
			arguments = Bundle().apply {
				putString("plantId", plantId)
				actionId?.let { putString("actionId", it) }
			}
		}
	}

	private val viewModel by viewModels<WateringViewModel> { viewModelFactory() }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		viewModel.initialise(requireArguments().getString("plantId")!!, arguments?.getString("actionId"))
		setupUi()
	}

	private fun setupUi()
	{
		fab_complete.setOnClickListener {
			viewModel.setValues(
				ph = water_ph.text.toDoubleOrNull(),
				tds = water_ppm.text.toDoubleOrNull()
			)
			requireActivity().finish()
		}

		viewModel.plant.observe(viewLifecycleOwner) { plant ->
			plant ?: return@observe
			requireActivity().title = getString(R.string.feeding_single_title, plant.name)
		}

		viewModel.action.observe(viewLifecycleOwner) { action ->
			action.ph?.let { ph -> water_ph.setText(ph.formatWhole()) }
			action.tds?.let { tds -> water_ppm.setText(tds.amount.formatWhole()) }
		}
	}
}
