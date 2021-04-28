package me.anon.grow3.ui.crud.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import me.anon.grow3.databinding.FragmentCrudDiaryCompleteBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.crud.viewmodel.DiaryCrudViewModel
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.component
import me.anon.grow3.util.navigateTo
import javax.inject.Inject

class DiaryCompleteFragment : BaseFragment(FragmentCrudDiaryCompleteBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var crudViewModelFactory: DiaryCrudViewModel.Factory
	private val crudViewModel: DiaryCrudViewModel by activityViewModels { ViewModelProvider(crudViewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryCompleteBinding>()

	init {
		lifecycleScope.launchWhenCreated {
			crudViewModel.completeCrud()
		}
	}

	override fun bindUi()
	{
		component.corePreferences().isFirstLaunch = false

		viewBindings.close.setOnClickListener {
			navigateTo<ViewDiaryFragment> {
				bundleOf(EXTRA_DIARY_ID to crudViewModel.diaryVm.diaryId)
			}

			requireActivity().finish()
		}
	}

	override fun onBackPressed(): Boolean
	{
		requireActivity().finish()
		return true
	}
}
