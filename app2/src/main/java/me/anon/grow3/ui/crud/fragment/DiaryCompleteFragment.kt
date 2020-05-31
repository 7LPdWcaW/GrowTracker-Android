package me.anon.grow3.ui.crud.fragment

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_crud_diary_complete.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.MainActivity
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.component
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.isSuccess
import javax.inject.Inject

class DiaryCompleteFragment : BaseFragment(R.layout.fragment_crud_diary_complete)
{
	override val inject: (ApplicationComponent) -> Unit = { component.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindUi()
	{
		close.setOnClickListener {
			navigateTo<MainActivity>()
		}
	}

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) {
			if (!it.isSuccess) return@observe
			viewModel.save(it.asSuccess())
		}
	}
}
