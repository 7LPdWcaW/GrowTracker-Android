package me.anon.grow3.ui.crud.fragment

import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_crud_diary_environment.*
import me.anon.grow3.R
import me.anon.grow3.data.model.EnvironmentType
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.ViewModelProvider
import javax.inject.Inject

class DiaryEnvironmentFragment : BaseFragment(R.layout.fragment_crud_diary_environment)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindUi()
	{
		environment_type_options.setMenu(EnvironmentType.toMenu())
	}

	override fun bindVm()
	{

	}
}
