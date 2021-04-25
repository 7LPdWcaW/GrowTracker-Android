package me.anon.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
	private val args: WateringFragmentArgs by navArgs()
	private val viewModel by viewModels<WateringViewModel> { viewModelFactory() }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		viewModel.initialise(args.plantIds.first(), args.plantActionId)
		setupUi()
	}

	private fun setupUi()
	{
		fab_complete.setOnClickListener {
			viewModel.setValues(
				ph = water_ph.text.toDoubleOrNull(),
				tds = water_ppm.text.toDoubleOrNull()
			)
			findNavController().navigateUp()
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
