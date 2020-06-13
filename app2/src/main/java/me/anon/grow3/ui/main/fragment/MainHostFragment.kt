package me.anon.grow3.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.viewpager2.widget.ViewPager2
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentMainHostBinding
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.diaries.fragment.LogListFragment
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_DIARY_ID
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_NAVIGATE
import me.anon.grow3.ui.main.activity.MainActivity.Companion.NAVIGATE_TO_CROPS
import me.anon.grow3.ui.main.activity.MainActivity.Companion.NAVIGATE_TO_DIARY
import me.anon.grow3.util.applyWindowInsets
import me.anon.grow3.util.applyWindowPaddings

class MainHostFragment : BaseHostFragment(FragmentMainHostBinding::class.java)
{
	private val pendingActions = ArrayList<Bundle>(1)
	private val viewPager by lazy { activity().findViewById<ViewPager2>(R.id.view_pager) }
	private val viewBindings by lazy { binding<FragmentMainHostBinding>() }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)
		applyWindowInsets(view)
		applyWindowInsets(viewBindings.menuFab, top = false)
		viewBindings.menuFab.post { viewBindings.menuFab.requestApplyInsets() }
		applyWindowPaddings(viewBindings.sheet)
		viewBindings.sheet.post { viewBindings.sheet.requestApplyInsets() }
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		if (activity !is MainActivity) throw IllegalStateException("Main host not attached to Main activity")

		if (pendingActions.isNotEmpty())
		{
			executePendingActions()
		}
		else if (savedInstanceState == null)
		{
			childFragmentManager.commit {
				replace(R.id.content, ViewDiaryFragment())
			}
		}
	}

	override fun setArguments(args: Bundle?)
	{
		super.setArguments(args)

		args?.let {
			pendingActions += it
			if (isAdded && !isDetached) executePendingActions()
		}
	}

	private fun executePendingActions()
	{
		with (pendingActions.asReversed())
		{
			while (size > 0)
			{
				val item = this.removeAt(0)
				val route = item.getString(EXTRA_NAVIGATE) ?: throw IllegalArgumentException("No route set")
				when (route)
				{
					NAVIGATE_TO_DIARY -> {
						openDiary(item.getString(EXTRA_DIARY_ID) ?: throw IllegalArgumentException("No diary ID set"))
						openDetail(null)
					}

					NAVIGATE_TO_CROPS -> {
						openDetail(LogListFragment().apply {
							arguments = item
						})
					}
				}
			}
		}
	}

	override fun onBackPressed(): Boolean = viewBindings.menuFab.isExpanded.also { viewBindings.menuFab.isExpanded = false }

	private fun openDetail(fragment: Fragment?)
	{
		activity().setDetail(fragment)
	}

	private fun openDiary(id: String)
	{
		val fragment = ViewDiaryFragment().apply {
			arguments = Bundle().apply {
				putString(ViewDiaryFragment.EXTRA_DIARY_ID, id)
			}
		}

		childFragmentManager.commit {
			setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
			replace(R.id.content, fragment)
			runOnCommit {
				activity().notifyPagerChange(this@MainHostFragment)

				viewBindings.menuFab.isVisible = true
				viewBindings.menuFab.setOnClickListener {
					viewBindings.menuFab.isExpanded = !viewBindings.menuFab.isExpanded
					viewPager.isUserInputEnabled = !viewBindings.menuFab.isExpanded
				}
				viewBindings.sheet.setOnClickListener {
					viewBindings.menuFab.isExpanded = false
					viewPager.isUserInputEnabled = true
				}
			}
		}
	}
}
