package me.anon.grow3.ui.crud.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import me.anon.grow3.databinding.FragmentCrudDiaryCompleteBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_DIARY_ID
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.component
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.states.asSuccess
import javax.inject.Inject

class DiaryCompleteFragment : BaseFragment(FragmentCrudDiaryCompleteBinding::class)
{
	override val inject: (ApplicationComponent) -> Unit = { component.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryCompleteBinding>()

	override fun bindUi()
	{
		viewBindings.close.setOnClickListener {
			navigateTo<ViewDiaryFragment> {
				bundleOf(EXTRA_DIARY_ID to viewModel.diary.value!!.asSuccess().id)
			}
		}
	}

	override fun bindVm()
	{
		viewModel.diary.value ?: throw IllegalArgumentException("No diary to save")
		viewModel.diary.value?.let {
			viewModel.save(it.asSuccess())
		}
	}
}
