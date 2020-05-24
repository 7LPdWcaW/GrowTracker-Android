package me.anon.grow3.ui.crud.fragment

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_crud_diary_details.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class DiaryDetailsFragment : BaseFragment(R.layout.fragment_crud_diary_details)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			date.editText!!.text = diary.date.asDateTime().asFormattedString().asEditable()
		}
	}

	override fun bindUi()
	{
		date.editText!!.onFocus {
			it.hideKeyboard()

			val current = viewModel.diary.value?.date ?: ZonedDateTime.now().asString()
			DateSelectDialogFragment.show(current, true, childFragmentManager).apply {
				onDateTimeSelected = ::onDateSelected
			}

			viewModel.setDiaryDate(current.asDateTime())
		}

		attachCallbacks()
	}

	private fun attachCallbacks()
	{
		DateSelectDialogFragment.attach(childFragmentManager, ::onDateSelected)
	}

	public fun onDateSelected(selectedDate: ZonedDateTime)
	{
		viewModel.setDiaryDate(selectedDate)
	}
}
