package me.anon.grow3.ui.crud.fragment

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_crud_diary_details.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.DataResult
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
			val diary = (diary as? DataResult.Success)?.data ?: return@observe
			diary_name.editText!!.text = diary.name.asEditable()
			date.editText!!.text = diary.date.asDateTime().asFormattedString().asEditable()
		}
	}

	override fun bindUi()
	{
		diary_name.editText!!.doAfterTextChanged {
			// don't re-trigger the text change by calling editText.text ...
			val diary = (viewModel.diary.value as? DataResult.Success)?.data ?: return@doAfterTextChanged
			diary.name = it.toString()
		}

		date.editText!!.onFocus {
			val diary = (viewModel.diary.value as? DataResult.Success)?.data ?: return@onFocus
			diary.name = it.toString()

			it.hideKeyboard()

			val current = diary.date
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
		if (date.editText?.focusSearch(View.FOCUS_RIGHT)?.requestFocus() != true) date.editText?.clearFocus()
	}
}
