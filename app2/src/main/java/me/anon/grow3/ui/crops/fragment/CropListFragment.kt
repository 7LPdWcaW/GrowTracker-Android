package me.anon.grow3.ui.crops.fragment

import androidx.fragment.app.viewModels
import com.freelapp.flowlifecycleobserver.collectWhileResumed
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.crops.view.CropCard
import me.anon.grow3.ui.crops.viewmodel.CropListViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import javax.inject.Inject

class CropListFragment : CardListFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: CropListViewModel.Factory
	private val viewModel: CropListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.state
			.collectWhileResumed(viewLifecycleOwner) {
				val result = it as? CropListViewModel.UiResult.Loaded ?: return@collectWhileResumed
				val cards = result.crops.map { crop ->
					CropCard(result.diary, crop)
				}

				viewAdapter.newStack { addAll(cards) }
			}
	}
}
