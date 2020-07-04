package me.anon.grow3.ui.main.fragment

import android.os.Bundle
import androidx.fragment.app.commitNow
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentMainHostBinding
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.diaries.fragment.EmptyFragment
import me.anon.grow3.ui.diaries.fragment.LogListFragment
import me.anon.grow3.ui.diaries.fragment.ViewCropFragment
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_NAVIGATE
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_ORIGINATOR
import me.anon.grow3.ui.main.activity.MainActivity.Companion.INDEX_MAIN
import me.anon.grow3.util.nameOf
import kotlin.random.Random

/**
 * Main navigator fragment for the application. [MainActivity] controls the UI and distribution
 * of navigation actions from this class.
 */
class MainNavigatorFragment : BaseHostFragment(FragmentMainHostBinding::class)
{
	private val pendingActions = ArrayList<Bundle>(1)

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
			arguments = null
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
					nameOf<ViewDiaryFragment>() -> {
						clearStack(true)
					}
				}

				// TODO: we should re-open a page if it exists in the stack,
				// but we cant because we clear the stack also. Possibly look
				// for the fragment in childFragmentManager first?
				item.putInt("nonce", Random.nextInt())

				when (route)
				{
					nameOf<LogActionBottomSheetFragment>() -> {
						activity().openSheet(LogActionBottomSheetFragment())
					}

					nameOf<EmptyFragment>() -> {
						beginStack(EmptyFragment::class.java, item)
					}

					nameOf<ViewDiaryFragment>() -> {
						item.getString(EXTRA_DIARY_ID) ?: throw IllegalArgumentException("No diary ID set")
						beginStack(ViewDiaryFragment::class.java, item)
					}

					nameOf<ViewCropFragment>() -> {
						addToStack(ViewCropFragment::class.java, item)
					}

					nameOf<LogListFragment>() -> {
						addToStack(LogListFragment::class.java, item)
					}
				}
			}
		}
	}

	override fun onBackPressed(): Boolean
		= (childFragmentManager.findFragmentByTag("fragment") as? BaseFragment)?.onBackPressed() ?: false

	private fun clearStack(now: Boolean)
	{
		activity().clearStack(now)
	}

	private fun beginStack(fragment: Class<out BaseFragment>, args: Bundle?)
	{
		clearStack(true)
		childFragmentManager.commitNow {
			setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
			replace(R.id.fragment_container, fragment.newInstance().apply {
				arguments = args
			}, "fragment")

			runOnCommit {
				activity().viewBindings.viewPager.post {
					activity().viewBindings.viewPager.setCurrentItem(INDEX_MAIN, true)
				}
			}
		}
	}

	private fun addToStack(fragment: Class<out BaseFragment>, args: Bundle?)
	{
		activity().addToStack(fragment, args)
	}
}
