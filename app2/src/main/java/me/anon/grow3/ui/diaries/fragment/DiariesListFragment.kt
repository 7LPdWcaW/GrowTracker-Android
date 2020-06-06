package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.R
import me.anon.grow3.ui.diaries.viewmodel.DiariesListViewModel
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.component
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

class DiariesListFragment : Fragment(R.layout.fragment_gardens)
{
	@Inject internal lateinit var viewModelFactory: DiariesListViewModel.Factory
	private val viewModel: DiariesListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		component.inject(this)

		requireView().setBackgroundColor(Random.nextLong().and(0xffffff).or(0xff000000).toInt())
		setupList()
	}

	private fun setupList()
	{
		viewModel.gardens.observe(viewLifecycleOwner) {
			Timber.d("$it")
		}
	}
}
