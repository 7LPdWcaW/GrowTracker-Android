package me.anon.grow3.ui.action.fragment

import androidx.core.view.plusAssign
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.textfield.TextInputLayout
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.Water
import me.anon.grow3.databinding.FragmentActionLogBinding
import me.anon.grow3.ui.action.view.LogView
import me.anon.grow3.ui.action.view.StageChangeLogView
import me.anon.grow3.ui.action.view.WaterLogView
import me.anon.grow3.ui.action.viewmodel.LogActionViewModel
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.util.*
import me.anon.grow3.view.CropSelectView
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

open class LogActionFragment : BaseFragment(FragmentActionLogBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: LogActionViewModel.Factory
	protected val viewModel: LogActionViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	protected val viewBindings by viewBinding<FragmentActionLogBinding>()
	private var logView: LogView<*>? = null
	private var isFinishing = false

	override fun bindUi()
	{
		setToolbar(viewBindings.toolbar)

		viewBindings.toolbar.setNavigationOnClickListener {
			requireActivity().promptExit {
				requireActivity().supportFragmentManager.commitNow {
					remove(this@LogActionFragment)
				}
			}
		}

		viewBindings.actionDone.onClick {
			logView?.let {
				it.saveView()
				viewModel.saveLog()
				finish()
			}
		}

		attachCallbacks()
	}

	override fun bindVm()
	{
		viewModel.log.observe(viewLifecycleOwner) { log ->
			val diary = viewModel.diary.value!!
			renderLogView(diary, log)
		}
	}

	open fun finish()
	{
		requireView().hideKeyboard()
		isFinishing = true

		activity?.supportFragmentManager?.commitNow {
			remove(this@LogActionFragment)
		}
	}

	override fun onDestroyView()
	{
		if (!isFinishing)
		{
			logView?.let {
				it.saveView()
				viewModel.saveLog(draft = true)
			}
		}

		super.onDestroyView()
	}

	private fun renderLogView(diary: Diary, log: Log)
	{
		logView = when (log)
		{
			is Water -> WaterLogView(diary, log)
			is StageChange -> StageChangeLogView(diary, log)
			else -> null
		}

		logView?.let { logView ->
			viewBindings.toolbar.title = logView.provideTitle() ?: R.string.log_action_new_title.string()

			viewBindings.logContent.removeAllViews()
			val view = logView.createView(layoutInflater, viewBindings.logContent)
			logView.bindView(view)
			viewBindings.logContent += view

			view.findViewById<TextInputLayout>(R.id.date)?.let {
				it.editText!!.onFocus {
					it.hideKeyboard()
					val current = diary.date
					DateSelectDialogFragment.show(current, true, childFragmentManager).apply {
						onDateTimeSelected = ::onDateSelected
						onDismiss = ::onDateDismissed
					}
				}
			}

			view.findViewById<CropSelectView>(R.id.crop_select_view)?.let {
				it.setDiary(diary)
			}
		}
	}

	private var toConfirm = true
	override fun onBackPressed(): Boolean
	{
		if (toConfirm)
		{
			requireActivity().promptExit {
				toConfirm = false
				activity?.onBackPressed()
			}
		}
		else
		{
			requireActivity().supportFragmentManager.commitNow {
				remove(this@LogActionFragment)
			}
		}

		return true
	}

	private fun attachCallbacks()
	{
		DateSelectDialogFragment.attach(childFragmentManager, ::onDateSelected, ::onDateDismissed)
	}

	public fun onDateSelected(selectedDate: ZonedDateTime)
	{
		requireView().findViewById<TextInputLayout>(R.id.date)?.let {
			it.editText!!.text = selectedDate.asNumericalString().asEditable()
		}
	}

	public fun onDateDismissed()
	{
		requireView().findViewById<TextInputLayout>(R.id.date)?.let {
			it.editText!!.clearFocus()
		}
	}
}
