package me.anon.grow3.ui.crud.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.coroutines.launch
import me.anon.grow3.R
import me.anon.grow3.databinding.ActivityCrudDiaryBinding
import me.anon.grow3.ui.action.fragment.LogActionFragment
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.onClick
import me.anon.grow3.util.promptExit
import javax.inject.Inject

/**
 * Wizard activity for creating a new diary
 */
class DiaryActivity : BaseActivity(ActivityCrudDiaryBinding::class)
{
	private var currentView = R.id.navigation_diary_details

	override val injector: Injector = { it.inject(this) }
	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<ActivityCrudDiaryBinding>()
	private val navController by lazy { findNavController(R.id.nav_host_fragment) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		navController.addOnDestinationChangedListener { _, destination, _ ->
			currentView = destination.id
			viewBindings.back.isVisible = currentView != R.id.navigation_diary_details
			viewBindings.next.isVisible = currentView != R.id.navigation_diary_crop
			viewBindings.done.isVisible = currentView == R.id.navigation_diary_crop

			if (currentView == R.id.navigation_diary_complete)
			{
				viewBindings.next.isVisible = currentView != R.id.navigation_diary_complete
				viewBindings.back.isVisible = currentView != R.id.navigation_diary_complete
				viewBindings.includeToolbar.toolbar?.isVisible = false
			}
		}

		NavigationUI.setupWithNavController(viewBindings.includeToolbar.toolbar, navController)
	}

	override fun bindUi()
	{
		viewBindings.done.setOnClickListener {
			navController.navigate(R.id.page_2_to_1)
		}

		viewBindings.back.onClick {
			if (!navController.popBackStack())
			{
				promptExit {
					lifecycleScope.launch {
						//viewModel.cancel()
						finish()
					}
				}
			}
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
		if (viewBindings.dialogHolder.isVisible)
		{
			supportFragmentManager.findFragmentById(R.id.dialog_fragment_container)?.let {
				if (it is LogActionFragment)
				{
					promptExit {
						supportFragmentManager.commitNow {
							remove(it)
						}
					}
				}
			}
		}
		else
		{
			viewBindings.back.performClick()
		}
	}

	override fun onFragmentAdded(fragment: Fragment)
	{
		if (fragment is LogActionFragment)
		{
			viewBindings.dialogHolder.isVisible = true
		}
	}

	override fun onFragmentRemoved(fragment: Fragment)
	{
		if (fragment is LogActionFragment)
		{
			viewBindings.dialogHolder.isVisible = false
		}
	}

	public fun openModal(fragment: Fragment)
	{
		supportFragmentManager.commitNow {
			replace(R.id.dialog_fragment_container, fragment)
		}
	}
}
