package me.anon.grow3.ui.action.fragment

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.view.plusAssign
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
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
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.util.*
import me.anon.grow3.view.CropSelectView
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

open class LogActionFragment : BaseFragment(FragmentActionLogBinding::class)
{
	companion object
	{
		public const val EXTRA_SINGLE_CROP = "logaction.single"
	}

	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: LogActionViewModel.Factory
	protected val viewModel: LogActionViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	protected val viewBindings by viewBinding<FragmentActionLogBinding>()
	private var logView: LogView<*>? = null
	private var isFinishing = false

	override fun bindArguments(bundle: Bundle?)
	{
		super.bindArguments(bundle)

		bundle?.getString(Extras.EXTRA_LOG_ID).let {
			if (it.isNullOrBlank()) viewModel.new()
			else viewModel.load(it)
		}
	}

	override fun bindUi()
	{
		setToolbar(viewBindings.toolbar)

		viewBindings.toolbar.setNavigationOnClickListener {
			onBackPressed()
		}

		viewBindings.actionDelete.isVisible = !viewModel.isNew
		viewBindings.actionDelete.onClick {
			requireActivity().promptRemove {
				viewModel.remove()
				finish()
			}
		}

		viewBindings.actionDone.onClick {
			isFinishing = true

			logView?.let {
				val log = it.saveView()

				requireView().findViewById<CropSelectView>(R.id.crop_select_view)?.let {
					log.cropIds = it.selectedCrops.toList()
				}

				viewModel.save(log)
				finish()
			}
		}

		attachCallbacks()
	}

	override fun bindVm()
	{
		viewModel.log
			.nonNull()
			.observe(viewLifecycleOwner) { data ->
				val diary = data.diary ?: return@observe
				val log = data.log ?: return@observe

				renderLogView(diary, log)
			}
	}

	open fun finish()
	{
		requireView().hideKeyboard()
		isFinishing = true
		viewModel.clear()

		activity?.supportFragmentManager?.commit {
			remove(this@LogActionFragment)
		}
	}

	override fun onDestroyView()
	{
//		if (!isFinishing)
//		{
//			logView?.let {
//				it.saveView()
//				viewModel.saveLog()
//			}
//		}

		super.onDestroyView()
	}

	private fun renderLogView(diary: Diary, log: Log)
	{
		viewBindings.actionDelete.isVisible = !viewModel.isNew

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
					val current = diary.date
					DateSelectDialogFragment.show(current, true, childFragmentManager).apply {
						onDateTimeSelected = ::onDateSelected
						onDismiss = ::onDateDismissed
					}
				}
			}

			view.findViewById<CropSelectView>(R.id.crop_select_view)?.let {
				it.selectedCrops = arguments?.getStringArray(Extras.EXTRA_CROP_IDS)
					?.asSequence()
					?.toHashSet()
					?: diary.crops
						.map { it.id }
						.toHashSet()

				it.setDiary(diary)
			}

			if (arguments?.getBoolean(EXTRA_SINGLE_CROP, false) == true)
			{
				view.findViewById<CropSelectView>(R.id.crop_select_view)?.isVisible = false
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
			viewModel.clear()
			isFinishing = true
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
			it.editText!!.text = selectedDate.asDisplayString().asEditable()
		}
	}

	public fun onDateDismissed()
	{
		requireView().findViewById<TextInputLayout>(R.id.date)?.let {
			it.editText!!.clearFocus()
		}
	}
}
