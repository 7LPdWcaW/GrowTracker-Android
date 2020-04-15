package me.anon.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.action_buttons_stub.view.*
import kotlinx.android.synthetic.main.plant_details_view.*
import kotlinx.android.synthetic.main.tabbed_fragment_holder.*
import me.anon.grow.R
import me.anon.lib.DateRenderer
import me.anon.lib.ext.asHtml
import me.anon.lib.ext.inflate
import me.anon.lib.ext.viewModelFactory
import me.anon.model.Water
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

	override fun onDestroy()
	{
		super.onDestroy()
		(requireActivity() as? PlantDetailsActivity2)?.apply {
			toolbar_layout.removeViews(1, toolbar_layout.childCount - 1)
		}
	}

	private fun setupUi()
	{
		(requireActivity() as? PlantDetailsActivity2)?.apply {
			toolbar_layout.removeViews(1, toolbar_layout.childCount - 1)
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

			last_feeding.isVisible = false
			plant.actions.lastOrNull { it is Water }?.let { populateLastWatering(it as Water) }
		}
	}

	private fun populateLastWatering(water: Water)
	{
		last_feeding.isVisible = true
		var summary: String = water.getSummary(requireContext())
		if (!TextUtils.isEmpty(water.notes))
		{
			summary += "<br /><br />"
			summary += water.notes
		}

		last_feeding_summary.text = summary.asHtml()

		val actionDate = Date(water.date)
		last_feeding_full_date.text = "${dateFormat.format(actionDate)} ${timeFormat.format(actionDate)}"
		last_feeding_date.text = getString(R.string.ago, "<b>${DateRenderer(activity).timeAgo(water.date.toDouble()).formattedDate}</b>").asHtml()
		duplicate_feeding.setOnClickListener {


			it.context.startActivity(Intent(it.context, WateringActivity2::class.java).apply {
				putExtra("plantId", viewModel.plantId)
				putExtra("actionId", water.id)
			})
		}
	}
}
