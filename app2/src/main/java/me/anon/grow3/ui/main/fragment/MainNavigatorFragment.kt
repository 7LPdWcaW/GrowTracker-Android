package me.anon.grow3.ui.main.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.anon.grow3.R
import me.anon.grow3.data.exceptions.GrowTrackerException.*
import me.anon.grow3.databinding.FragmentMainHostBinding
import me.anon.grow3.ui.action.fragment.DeleteActionFragment
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.crops.fragment.CropListFragment
import me.anon.grow3.ui.crops.fragment.ViewCropFragment
import me.anon.grow3.ui.diaries.fragment.EmptyFragment
import me.anon.grow3.ui.diaries.fragment.LoadingFragment
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment
import me.anon.grow3.ui.logs.fragment.LogListFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_CLEAR
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_NAVIGATE
import me.anon.grow3.ui.main.activity.MainActivity.Companion.EXTRA_ORIGINATOR
import me.anon.grow3.ui.main.activity.MainActivity.Companion.INDEX_MAIN
import me.anon.grow3.ui.main.viewmodel.MainViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.nameOf
import me.anon.grow3.util.navigateTo
import javax.inject.Inject
import kotlin.random.Random

/**
 * Main navigator fragment for the application. [MainActivity] controls the UI and distribution
 * of navigation actions from this class.
 */
class MainNavigatorFragment : BaseHostFragment(FragmentMainHostBinding::class)
{
	private val pendingActions = ArrayList<Bundle>(1)
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: MainViewModel.Factory
	private val viewModel: MainViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this, arguments) }

	init {
		lifecycleScope.launch {
			whenStarted {
				viewModel.state.collectLatest { state ->
					when (state)
					{
						is MainViewModel.UiState.Loading -> navigateTo<LoadingFragment>()
						is MainViewModel.UiState.EmptyDiaries -> navigateTo<EmptyFragment>()
						is MainViewModel.UiState.ViewDiary -> navigateTo<ViewDiaryFragment> { bundleOf(EXTRA_DIARY_ID to state.diaryId) }
					}
				}
			}
		}
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		if (activity !is MainActivity) throw InvalidHostActivity()

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
			super.setArguments(null)
			if (isAdded && !isDetached) executePendingActions()
		}
	}

	/**
	 * The main navigation controller method. All routings happen here
	 */
	private fun executePendingActions()
	{
		with (pendingActions.asReversed()) {
			while (size > 0)
			{
				val item = this.removeAt(0)
				val route = item.getString(EXTRA_NAVIGATE) ?: throw NoRoute()
				val origin = item.getString(EXTRA_ORIGINATOR)
				val clear = item.getBoolean(EXTRA_CLEAR, false)

				when (origin)
				{
					nameOf<ViewDiaryFragment>() -> {
						clearStack(true)
					}
				}

				if (clear) clearStack(false)

				// TODO: we should re-open a page if it exists in the stack,
				// but we cant because we clear the stack also. Possibly look
				// for the fragment in childFragmentManager first?
				item.putInt("nonce", Random.nextInt())

				when (route)
				{
					nameOf<LogActionBottomSheetFragment>() -> {
						activity().openSheet(LogActionBottomSheetFragment().apply {
							arguments = item
						})
					}

					nameOf<DeleteActionFragment>() -> {
						childFragmentManager.commitNow {
							setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
							add(R.id.fragment_container, DeleteActionFragment().apply {
								arguments = item
							}, "delete-action")
						}
					}

					nameOf<LoadingFragment>() -> {
						beginStack(LoadingFragment::class.java, null)
					}

					nameOf<EmptyFragment>() -> {
						beginStack(EmptyFragment::class.java, item)
					}

					nameOf<ViewDiaryFragment>() -> {
						item.getString(EXTRA_DIARY_ID) ?: throw InvalidDiaryId()
						beginStack(ViewDiaryFragment::class.java, item)
					}

					nameOf<ViewCropFragment>() -> {
						addToStack(ViewCropFragment::class.java, item)
					}

					nameOf<CropListFragment>() -> {
						addToStack(CropListFragment::class.java, item)
					}

					nameOf<LogListFragment>() -> {
						addToStack(LogListFragment::class.java, item)
					}

					else -> throw InvalidRoute(route)
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
		//clearStack(true)
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
