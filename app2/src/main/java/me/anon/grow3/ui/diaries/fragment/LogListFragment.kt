package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.observe
import me.anon.grow3.databinding.FragmentViewDiaryLogsBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.Injector
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick
import me.anon.grow3.util.updateMargin

class LogListFragment : BaseFragment(FragmentViewDiaryLogsBinding::class)
{
	override val injector: Injector = {}
	private val viewBindings by viewBinding<FragmentViewDiaryLogsBinding>()

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		setToolbar(viewBindings.includeToolbar.toolbar)
		viewBindings.includeToolbar.toolbar.setNavigationOnClickListener {
			(requireActivity() as MainActivity).clearStack()
		}

		insets.observe(viewLifecycleOwner) {
			viewBindings.includeToolbar.toolbar.updateMargin(it.left, it.top, it.right)
		}

		viewBindings.test.onClick {
			navigateTo<LogListFragment> {
				bundleOf(EXTRA_DIARY_ID to "")
			}
		}
	}

	override fun bindUi()
	{

	}

	override fun bindVm()
	{

	}
}
