package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import me.anon.grow3.databinding.FragmentViewDiaryLogsBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.applyWindowInsets
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick
import me.anon.grow3.util.updateMargin

class LogListFragment : BaseFragment(FragmentViewDiaryLogsBinding::class)
{
	override val inject: (ApplicationComponent) -> Unit = {}
	private val viewBindings by viewBinding<FragmentViewDiaryLogsBinding>()

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		setToolbar(viewBindings.includeToolbar.toolbar)
		viewBindings.includeToolbar.toolbar.setNavigationOnClickListener {
			(requireActivity() as MainActivity).clearStack()
		}

		applyWindowInsets(
			viewBindings.includeToolbar.toolbar
		) { v, l, t, r, b ->
			v.updateMargin(l, t, r)
		}

		viewBindings.test.onClick {
			navigateTo<LogListFragment> {
				bundleOf(MainActivity.EXTRA_DIARY_ID to "")
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
