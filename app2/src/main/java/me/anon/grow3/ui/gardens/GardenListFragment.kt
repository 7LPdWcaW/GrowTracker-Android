package me.anon.grow3.ui.gardens

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.R
import me.anon.grow3.ui.gardens.viewmodel.GardenListViewModel
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.component
import timber.log.Timber
import javax.inject.Inject

class GardenListFragment : Fragment(R.layout.fragment_gardens)
{
	@Inject internal lateinit var viewModelFactory: GardenListViewModel.Factory
	private val viewModel: GardenListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		component.inject(this)

		assert(component.gardenRepo() != null)

		setupList()
	}

	private fun setupList()
	{
		viewModel.gardens.observe(viewLifecycleOwner) {
			Timber.d("$it")
		}
	}
}
