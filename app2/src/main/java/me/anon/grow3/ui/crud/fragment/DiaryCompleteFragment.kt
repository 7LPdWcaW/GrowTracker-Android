package me.anon.grow3.ui.crud.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import me.anon.grow3.databinding.FragmentCrudDiaryCompleteBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.crud.viewmodel.DiaryCrudViewModel
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.navigateTo
import javax.inject.Inject

class DiaryCompleteFragment : BaseFragment(FragmentCrudDiaryCompleteBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var crudViewModelFactory: DiaryCrudViewModel.Factory
	private val crudViewModel: DiaryCrudViewModel by activityViewModels { ViewModelProvider(crudViewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryCompleteBinding>()

	override fun bindUi()
	{
		viewBindings.close.setOnClickListener {
			navigateTo<ViewDiaryFragment> {
				bundleOf(EXTRA_DIARY_ID to crudViewModel.diaryVm.diary.value!!.id)
			}

			requireActivity().finish()
		}
	}

	override fun onBackPressed(): Boolean
	{
		requireActivity().finish()
		return true
	}

	override fun bindVm()
	{
		/*crudViewModel.diaryVm.diary.value ?: throw IllegalArgumentException("No diary to save")
		crudViewModel.diaryVm.diary.value?.let {
			crudViewModel.diaryVm.save(it)
		}*/
	}
}
