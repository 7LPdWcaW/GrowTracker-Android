package me.anon.grow3.ui.main.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentMainHostBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.diaries.fragment.DiariesListFragment
import me.anon.grow3.ui.diaries.fragment.LogListFragment
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_DIARY_ID
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_NAVIGATE
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_ORIGINATOR
import me.anon.grow3.ui.main.activity.MainActivity.Companion.NAVIGATE_TO_CROPS
import me.anon.grow3.ui.main.activity.MainActivity.Companion.NAVIGATE_TO_DIARY

/**
 * Main navigator fragment for the application. [MainActivity] controls the UI and distribution
 * of navigation actions from this class.
 */
class MainNavigationFragment : BaseHostFragment(FragmentMainHostBinding::class.java)
{
	private val pendingActions = ArrayList<Bundle>(1)
	private val viewBindings by lazy { binding<FragmentMainHostBinding>() }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		if (activity !is MainActivity) throw IllegalStateException("Main host not attached to Main activity")

		if (pendingActions.isNotEmpty())
		{
			executePendingActions()
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

	/**
	 * The main navigation controller method. All routings happen here
	 */
	private fun executePendingActions()
	{
		with (pendingActions.asReversed())
		{
			while (size > 0)
			{
				val item = this.removeAt(0)
				val route = item.getString(EXTRA_NAVIGATE) ?: throw IllegalArgumentException("No route set")
				val origin = item.getString(EXTRA_ORIGINATOR)

				when (origin)
				{
					DiariesListFragment::class.java.name,
					ViewDiaryFragment::class.java.name -> {
						clearStack()
					}
				}

				when (route)
				{
					NAVIGATE_TO_DIARY, ViewDiaryFragment::class.java.name -> {
						openDiary(item.getString(EXTRA_DIARY_ID) ?: throw IllegalArgumentException("No diary ID set"))
					}

					NAVIGATE_TO_CROPS, LogListFragment::class.java.name -> {
						openDetail(LogListFragment().apply {
							arguments = item
						})
					}
				}
			}
		}
	}

	override fun onBackPressed(): Boolean
		= (childFragmentManager.findFragmentByTag("fragment") as? BaseFragment)?.onBackPressed() ?: false

	private fun clearStack()
	{
		activity().clearStack()
	}

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

		childFragmentManager.commitNow {
			setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
			replace(R.id.fragment_container, fragment, "fragment")
			activity().notifyPagerChange(this@MainNavigationFragment)
		}
	}
}
