package me.anon.grow3.ui.crud.fragment

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import me.anon.grow3.databinding.FragmentCrudDiaryDetailsBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.DataResult
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class DiaryDetailsFragment : BaseFragment(FragmentCrudDiaryDetailsBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryDetailsBinding>()

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			val diary = (diary as? DataResult.Success)?.data ?: return@observe
			viewBindings.diaryName.editText!!.text = diary.name.asEditable()
			viewBindings.date.editText!!.text = diary.date.asDateTime().asFormattedString().asEditable()
		}
	}

	override fun bindUi()
	{
		viewBindings.diaryName.editText!!.doAfterTextChanged {
			// don't re-trigger the text change by calling editText.text ...
			val diary = (viewModel.diary.value as? DataResult.Success)?.data ?: return@doAfterTextChanged
			diary.name = it.toString()
		}

		viewBindings.date.editText!!.onFocus {
			val diary = (viewModel.diary.value as? DataResult.Success)?.data ?: return@onFocus

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
		DateSelectDialogFragment.attach(childFragmentManager, ::onDateSelected, ::onDateDismissed)
	}

	public fun onDateSelected(selectedDate: ZonedDateTime)
	{
		viewModel.setDiaryDate(selectedDate)
	}

	public fun onDateDismissed()
	{
		if (viewBindings.date.editText?.focusSearch(View.FOCUS_RIGHT)?.requestFocus() != true) viewBindings.date.editText?.clearFocus()
	}
}
