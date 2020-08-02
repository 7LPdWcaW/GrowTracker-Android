package me.anon.grow3.ui.action.fragment

import androidx.core.view.plusAssign
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.Water
import me.anon.grow3.databinding.FragmentActionLogBinding
import me.anon.grow3.ui.action.view.LogView
import me.anon.grow3.ui.action.view.WaterLogView
import me.anon.grow3.ui.action.viewmodel.LogActionViewModel
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.*
import me.anon.grow3.view.CropSelectView
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
	}

	open fun finish()
	{
		requireView().hideKeyboard()

		isFinishing = true
	}

	override fun bindVm()
	{
		viewModel.log.observe(viewLifecycleOwner) { log ->
			val diary = viewModel.diary.value!!
			renderLogView(diary, log)
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
		when (log)
		{
			is Water -> {
				logView = WaterLogView(log)
			}
		}

		logView?.let { logView ->
			viewBindings.toolbar.title = logView.provideTitle() ?: R.string.log_action_new_title.string()

			viewBindings.logContent.removeAllViews()
			val view = logView.createView(layoutInflater, viewBindings.logContent)
			logView.bindView(view)
			viewBindings.logContent += view

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
}
