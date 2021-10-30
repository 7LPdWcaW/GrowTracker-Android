package me.anon.grow3.ui.crud.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.freelapp.flowlifecycleobserver.collectWhileStarted
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentCrudDiaryCompleteBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.crud.viewmodel.DiaryCrudViewModel
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.util.*
import javax.inject.Inject

class DiaryCompleteFragment : BaseFragment(FragmentCrudDiaryCompleteBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var crudViewModelFactory: DiaryCrudViewModel.Factory
	private val crudViewModel: DiaryCrudViewModel by activityViewModels { ViewModelProvider(crudViewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryCompleteBinding>()

	init {
		lifecycleScope.launchWhenCreated {
			crudViewModel.complete()
			component.corePreferences().isFirstLaunch = false
		}
	}

	override fun bindUi()
	{
		baseActivity.statusBarColor = R.attr.colorSecondary.resColor(requireContext())
	}

	override fun bindVm()
	{
		crudViewModel.state
			.collectWhileStarted(this) { state ->
				val diary = (state as? DiaryCrudViewModel.UiResult.Loaded)?.diary ?: return@collectWhileStarted
				viewBindings.close.setOnClickListener {
					navigateTo<ViewDiaryFragment>(clearTask = true) {
						bundleOf(EXTRA_DIARY_ID to diary.id)
					}

					requireActivity().finish()
				}
			}
	}

	override fun onBackPressed(): Boolean
	{
		requireActivity().finish()
		return true
	}
}
