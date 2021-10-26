package me.anon.grow3.ui.action.fragment

import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import com.freelapp.flowlifecycleobserver.collectWhileStarted
import me.anon.grow3.ui.action.viewmodel.DeleteActionViewModel
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.promptRemove
import javax.inject.Inject

class DeleteActionFragment : BaseFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DeleteActionViewModel.Factory
	protected val viewModel: DeleteActionViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.state
			.collectWhileStarted(this) { state ->
				when (state)
				{
					is DeleteActionViewModel.UiResult.Confirm -> {
						promptRemove {
							if (it) viewModel.remove()
						}
					}

					is DeleteActionViewModel.UiResult.Deleted -> {
						parentFragmentManager.commitNow {
							remove(this@DeleteActionFragment)
						}
					}
				}
			}
	}
}