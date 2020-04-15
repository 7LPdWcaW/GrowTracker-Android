package me.anon.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.plant_list_view.*
import me.anon.grow.R
import me.anon.lib.ext.removeAllItemDecorators
import me.anon.view.MainApplication2
import me.anon.view.PlantDetailsActivity2
import me.anon.view.SomeDividerItemDecoration
import me.anon.view.adapter.PlantsAdapter
import me.anon.view.viewmodel.PlantListViewModel
import me.anon.view.viewmodel.ViewModelFactory

/**
 * // TODO: Add class description
 */
class PlantListFragment : Fragment()
{
	companion object
	{
		public const val REQUEST_NEW_PLANT = 1
	}

	private val viewModel: PlantListViewModel by viewModels { ViewModelFactory(requireActivity().application as MainApplication2, this) }
	private val adapter = PlantsAdapter()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.plant_list_view, container, false)

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		setupUi()
		setupLoader()
		setupList()
		setupMenu()
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
	{
		inflater.inflate(R.menu.plant_list_menu, menu)

		val checkedFilters = viewModel.filters
		viewModel.allFilters.forEachIndexed { index, filter ->
			menu.findItem(R.id.filter).subMenu.add(R.id.filter_group, filter.hashCode(), index, filter.stage.printString).apply {
				isCheckable = true
				isChecked = checkedFilters.any { it == filter } == true
			}
		}

		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		if (item.groupId == R.id.filter_group)
		{
			val filter = viewModel.allFilters.find { it.hashCode() == item.itemId }
			filter ?: return false
			if (item.isChecked) viewModel.removeFilter(filter) else viewModel.applyFilter(filter)
			return true
		}

		return super.onOptionsItemSelected(item)
	}

	private fun setupMenu()
	{

	}

	private fun setupUi()
	{
		fab_add.setOnClickListener {
			startActivityForResult(Intent(it.context, PlantDetailsActivity2::class.java), REQUEST_NEW_PLANT)
		}
	}

	private fun setupLoader()
	{
		viewModel.loaded.observe(viewLifecycleOwner) { loaded ->
			content_container.isVisible = loaded
			progress_bar.isVisible = !loaded
		}
	}

	private fun setupList()
	{
		recycler_view.adapter = adapter
		recycler_view.layoutManager = LinearLayoutManager(requireContext())
		recycler_view.removeAllItemDecorators()
		recycler_view.addItemDecoration(SomeDividerItemDecoration(
			requireActivity(),
			SomeDividerItemDecoration.VERTICAL,
			R.drawable.divider_8dp,
			showDivider = { index, _, adapter -> adapter.getItemViewType(index) != PlantsAdapter.TYPE_HIDDEN }
		))

		viewModel.plants.observe(viewLifecycleOwner) { plants ->
			adapter.items = plants

			empty.isVisible = adapter.itemCount == 0
			recycler_view.isVisible = adapter.itemCount != 0
		}
	}
}
