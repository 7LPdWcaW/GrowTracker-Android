package me.anon.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.action_buttons_stub.view.*
import kotlinx.android.synthetic.main.plant_details_view.*
import kotlinx.android.synthetic.main.tabbed_fragment_holder.*
import me.anon.grow.R
import me.anon.lib.ext.inflate
import me.anon.lib.ext.removeViewsFrom
import me.anon.lib.ext.viewModelFactory
import me.anon.view.PlantDetailsActivity2
import me.anon.view.WateringActivity2
import me.anon.view.viewmodel.PlantDetailsViewModel
import java.util.*

/**
 * // TODO: Add class description
 */
class PlantDetailsFragment : Fragment(R.layout.plant_details_view)
{
	companion object
	{
		public const val EXTRA_PLANT_ID = "plantId"

		public fun newInstance(plantId: String? = null): PlantDetailsFragment = PlantDetailsFragment().apply {
			arguments = Bundle().apply {
				plantId?.let { putString(EXTRA_PLANT_ID, it) }
			}
		}
	}

	private val viewModel: PlantDetailsViewModel by viewModels { viewModelFactory() }
	private val dateFormat by lazy { DateFormat.getDateFormat(activity) }
	private val timeFormat by lazy { DateFormat.getTimeFormat(activity) }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		viewModel.plantId = arguments?.getString(EXTRA_PLANT_ID)
		viewModel.initialise()

		setupUi()
		setupDetails()
	}

	private fun setupUi()
	{
		(requireActivity() as? PlantDetailsActivity2)?.apply {
			toolbar_layout.removeViewsFrom(1)
			toolbar_layout.inflate<View>(R.layout.action_buttons_stub, true)

			toolbar_layout.feeding?.setOnClickListener {
				it.context.startActivity(Intent(it.context, WateringActivity2::class.java).apply {
					putExtra("plantId", viewModel.plantId)
				})
			}
		}

		fab_complete.setOnClickListener {
			viewModel.name.value = plant_name.text.toString()
			viewModel.strain.value = plant_strain.text.toString()
			viewModel.savePlant()
			requireActivity().finish()
		}
	}

	private fun setupDetails()
	{
		viewModel.plant.observe(viewLifecycleOwner) { plant ->
			requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
			requireActivity().title = getString(R.string.plant_details_title)

			plant.name.let { plant_name.setText(it) }
			plant.strain?.let { plant_strain.setText(it) }

			val dateStr = "${dateFormat.format(Date(plant.plantDate))} ${timeFormat.format(Date(plant.plantDate))}"
			plant_date.text = dateStr
			from_clone.isChecked = plant.clone
		}
	}
}
