package me.anon.grow3.ui.action.fragment

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.plusAssign
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Environment
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.asView
import me.anon.grow3.databinding.FragmentActionLogBinding
import me.anon.grow3.ui.action.view.LogView
import me.anon.grow3.ui.action.viewmodel.LogActionViewModel
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.base.ModalFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.util.*
import me.anon.grow3.view.LogCommonView
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

open class LogActionFragment : BaseFragment(FragmentActionLogBinding::class), ModalFragment
{
	companion object
	{
		public const val EXTRA_SINGLE_CROP = "logaction.single"
		public const val EXTRA_COPY = "log.copy"
	}

	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: LogActionViewModel.Factory
	protected val viewModel: LogActionViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	protected val viewBindings by viewBinding<FragmentActionLogBinding>()
	private var logView: LogView<*>? = null
	private var isFinishing = false

	override var completionListener: (() -> Unit)? = null
	public var intentCallback: ((Intent) -> Unit)? = null

	public val intentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		val resultCode = result.resultCode
		val data = result.data

		if (resultCode == Activity.RESULT_OK)
		{
			intentCallback?.invoke(data!!)
			logView?.saveAdapter(viewBindings.logContent[0])
			logView?.bindAdapter(viewBindings.logContent[0])
			intentCallback = null
		}
	}

	override fun onDestroy()
	{
		super.onDestroy()
		intentCallback = null
		completionListener = null
	}

//	override fun bindArguments(bundle: Bundle?)
//	{
//		super.bindArguments(bundle)
//
//		bundle?.getString(Extras.EXTRA_LOG_ID).let {
//			if (it.isNullOrBlank()) viewModel.new()
//			else viewModel.load(it)
//		}
//	}

	public fun saveView()
	{
		if (isFinishing) return

		logView?.let { logView ->
			val log = logView.saveAdapter(viewBindings.logContent[0])
			(viewBindings.logContent[1] as? LogCommonView)?.saveTo(log)

			requireView().clearFocus()
			viewModel.save(log)
		}
	}

	override fun bindUi()
	{
		setToolbar(viewBindings.toolbar)

		viewBindings.toolbar.setNavigationOnClickListener {
			onBackPressed()
		}

		//viewBindings.actionDelete.isVisible = !viewModel.isNew
		viewBindings.actionDelete.onClick {
			requireActivity().promptRemove {
				if (it)
				{
					viewModel.remove()
					finish()
				}
			}
		}

		viewBindings.actionDone.onClick {
			logView?.let { logView ->
				val log = logView.saveAdapter(viewBindings.logContent[0])
				(viewBindings.logContent[1] as? LogCommonView)?.saveTo(log)

				requireView().clearFocus()
				viewModel.saveAndFinish(log)
			}
		}

		attachCallbacks()
	}

	override fun bindVm()
	{
		lifecycleScope.launchWhenStarted {
			viewModel.state
				.collectLatest { state ->
					when (state)
					{
						is LogActionViewModel.UiResult.Loading -> {
							viewBindings.logContent.isVisible = false
							viewBindings.progress.isVisible = true
						}

						is LogActionViewModel.UiResult.Loaded -> {
							renderLogView(state.diary, state.log)
						}

						is LogActionViewModel.UiResult.Finishing -> {
							viewBindings.logContent.isVisible = false
							viewBindings.progress.isVisible = false
							finish()
						}
					}
				}
		}
	}

	open fun finish()
	{
		requireView().hideKeyboard()
		isFinishing = true
		viewModel.clear()
		completionListener?.invoke()
	}

	private fun renderLogView(diary: Diary, log: Log)
	{
		viewBindings.actionDelete.isVisible = !viewModel.isNew

		logView = log.asView(diary)
		logView?.let { logView ->
			viewBindings.toolbar.title = logView.provideTitle() ?: R.string.log_action_new_title.string()

			viewBindings.logContent.removeAllViews()
			val view = logView.createView(layoutInflater, viewBindings.logContent)
			viewBindings.logContent += view.root
			logView.bindAdapter(view.root)

			val logCommon = LogCommonView(requireContext())
			logCommon.dateChangePrompt = {
				val current = diary.date
				DateSelectDialogFragment.show(current, true, childFragmentManager).apply {
					onDateTimeSelected = ::onDateSelected
					onDismiss = ::onDateDismissed
				}
			}
			logCommon.setLog(diary, log)
			logCommon.cropIds = arguments?.getStringArray(Extras.EXTRA_CROP_IDS)?.asSequence()?.toHashSet() ?: hashSetOf()

			if (arguments?.getBoolean(EXTRA_SINGLE_CROP, false) == true
				|| diary.crops.isEmpty()
				|| log is Environment)
			{
				logCommon.cropSelectViewVisible = false
			}

			viewBindings.logContent.addView(logCommon, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
			viewBindings.logContent.isVisible = true
			viewBindings.progress.isVisible = false
		}
	}

	private var toConfirm = true
	override fun onBackPressed(): Boolean
	{
		if (toConfirm)
		{
			requireActivity().promptExit {
				if (it)
				{
					toConfirm = false
					activity?.onBackPressed()
				}
			}
		}
		else
		{
			finish()
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
