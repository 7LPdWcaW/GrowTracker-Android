package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import me.anon.grow3.databinding.FragmentViewDiaryLogsBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.applyWindowInsets
import me.anon.grow3.util.updateMargin

class LogListFragment : BaseFragment(FragmentViewDiaryLogsBinding::class)
{
	override val inject: (ApplicationComponent) -> Unit = {}
	private val viewBindings by lazy { binding<FragmentViewDiaryLogsBinding>() }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		setToolbar(viewBindings.includeToolbar.toolbar)

		applyWindowInsets(
			viewBindings.includeToolbar.toolbar
		) { v, l, t, r, b ->
			v.updateMargin(l, t, r)
		}
	}

	override fun bindUi()
	{

	}

	override fun bindVm()
	{

	}
}
