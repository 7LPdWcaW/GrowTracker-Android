package me.anon.grow3.ui.crud.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.freelapp.flowlifecycleobserver.collectWhileStarted
import kotlinx.coroutines.launch
import me.anon.grow3.R
import me.anon.grow3.databinding.ActivityCrudDiaryBinding
import me.anon.grow3.ui.action.fragment.LogActionFragment
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.base.ModalFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.crud.viewmodel.DiaryCrudViewModel
import me.anon.grow3.util.*
import javax.inject.Inject

/**
 * Wizard activity for creating a new diary
 */
class DiaryActivity : BaseActivity(ActivityCrudDiaryBinding::class)
{
	private var currentView = R.id.navigation_diary_details

	override val injector: Injector = { it.inject(this) }
	@Inject internal lateinit var crudViewModelFactory: DiaryCrudViewModel.Factory
	private val viewModel: DiaryCrudViewModel by viewModels { ViewModelProvider(crudViewModelFactory, this) }
	private val viewBindings by viewBinding<ActivityCrudDiaryBinding>()
	private val navController by lazy { findNavController(R.id.nav_host_fragment) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		(intent.extras ?: savedInstanceState ?: bundleOf()).getString(Extras.EXTRA_DIARY_ID).let {
			if (it.isNullOrBlank()) viewModel.newDiary()
			else viewModel.loadDiary(it)
		}

		navController.addOnDestinationChangedListener { _, destination, args ->
			currentView = destination.id
			viewBindings.back.isVisible = currentView != R.id.navigation_diary_details && currentView != R.id.navigation_diary_crop
			viewBindings.next.isVisible = currentView != R.id.navigation_diary_crop && viewModel.diaryDraft

			if (currentView == R.id.navigation_diary_complete)
			{
				viewBindings.next.isVisible = currentView != R.id.navigation_diary_complete && viewModel.diaryDraft
				viewBindings.back.isVisible = currentView != R.id.navigation_diary_complete
				viewBindings.includeToolbar.toolbar.isVisible = false
			}
		}

		NavigationUI.setupWithNavController(viewBindings.includeToolbar.toolbar, navController)
	}

	public fun popBackStack()
	{
		navController.popBackStack()
	}

	override fun bindVm()
	{
		viewModel.state
			.collectWhileStarted(this) { state ->
				viewBindings.next.isVisible = currentView != R.id.navigation_diary_crop && viewModel.diaryDraft

				if (state is DiaryCrudViewModel.UiResult.Finishing)
				{
					finish()
				}
			}
	}

	override fun bindUi()
	{
		viewBindings.back.onClick {
			onBackPressed()
		}

		viewBindings.next.onClick {
			when (currentView)
			{
				R.id.navigation_diary_details -> navController.navigate(R.id.page_1_to_3)
				R.id.navigation_diary_environment -> navController.navigate(R.id.page_3_to_4)
				R.id.navigation_diary_complete -> finish()
			}
		}
	}

	override fun onBackPressed()
	{
		supportFragmentManager.findFragmentByTag("modal")?.let {
			if ((it as? BaseFragment)?.onBackPressed() == true) return@onBackPressed
		}

		val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
		val current = host?.childFragmentManager?.primaryNavigationFragment

		if ((current as? BaseFragment)?.onBackPressed() != true)
		{
			if (!navController.popBackStack())
			{
				if (viewModel.diaryDraft)
				{
					promptExit {
						if (it)
						{
							lifecycleScope.launch {
								viewModel.remove()
								finish()
							}
						}
					}
				}
				else
				{
					clearFocus()
					viewModel.complete()
					finish()
				}
			}
		}
	}

	override fun onFragmentAdded(fragment: Fragment)
	{
		if (supportFragmentManager.findFragmentById(R.id.dialog_fragment_container) == fragment)
		{
			viewBindings.dialogHolder.alpha = 0f
			viewBindings.dialogHolder.isVisible = true
			viewBindings.dialogHolder.animate()
				.alpha(1.0f)
				.setDuration(300L)
				.setListener(null)
				.start()
		}
	}

	override fun onFragmentRemoved(fragment: Fragment)
	{
		if (fragment is LogActionFragment)
		{
			viewBindings.dialogHolder.alpha = 1f
			viewBindings.dialogHolder.animate()
				.alpha(0.0f)
				.setDuration(300L)
				.setListener(object : AnimatorListenerAdapter()
				{
					override fun onAnimationEnd(animation: Animator)
					{
						viewBindings.dialogHolder.isVisible = false
					}
				})
				.start()
		}
	}

	public fun <T> openModal(fragment: T) where T : Fragment, T : ModalFragment
	{
		fragment.completionListener = {
			supportFragmentManager.commitNow {
				remove(fragment)
			}
		}

		supportFragmentManager.commitNow {
			replace(R.id.dialog_fragment_container, fragment, "modal")
		}
	}
}
