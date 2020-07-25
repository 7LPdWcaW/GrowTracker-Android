package me.anon.grow3.ui.crops.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.crops.view.CropCard
import me.anon.grow3.ui.crops.viewmodel.CropListViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.states.asSuccess
import javax.inject.Inject

class CropListFragment : CardListFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: CropListViewModel.Factory
	private val viewModel: CropListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.crops.observe(viewLifecycleOwner) {
			val cards = it.map {
				CropCard(viewModel.diary.value!!.asSuccess(), it)
			}
			viewAdapter.newStack { addAll(cards) }
		}
	}
}
