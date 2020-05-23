package me.anon.grow3.ui.crud.fragment

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_crud_diary_details.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.asDateTime
import me.anon.grow3.util.asEditable
import me.anon.grow3.util.asFormattedString
import javax.inject.Inject

class DiaryDetailsFragment : BaseFragment(R.layout.fragment_crud_diary_details)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindUi()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			date.editText!!.text = diary.date.asDateTime().asFormattedString().asEditable()
		}
	}
}
