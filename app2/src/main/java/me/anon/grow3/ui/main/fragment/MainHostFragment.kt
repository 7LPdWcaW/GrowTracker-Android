package me.anon.grow3.ui.main.fragment

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_host.*
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.diaries.fragment.LogListFragment
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_DIARY_ID
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_NAVIGATE
import me.anon.grow3.ui.main.activity.MainActivity.Companion.NAVIGATE_TO_CROPS
import me.anon.grow3.ui.main.activity.MainActivity.Companion.NAVIGATE_TO_DIARY

class MainHostFragment : BaseHostFragment(R.layout.fragment_main_host)
{
	private val pendingActions = ArrayList<Bundle>(1)
	private val viewPager by lazy { activity().view_pager }

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

		viewPager.setOnApplyWindowInsetsListener { v, insets ->
			v.onApplyWindowInsets(insets).also {
				val navigationBar = insets.systemWindowInsetBottom
				menu_fab.translationY = (-navigationBar).toFloat()
				sheet.updatePadding(bottom = navigationBar)
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

	override fun onBackPressed(): Boolean = menu_fab.isExpanded.also { menu_fab.isExpanded = false }

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

				menu_fab.isVisible = true
				menu_fab.setOnClickListener { menu_fab.isExpanded = !menu_fab.isExpanded }
				sheet.setOnClickListener { menu_fab.isExpanded = false }
			}
		}
	}
}
