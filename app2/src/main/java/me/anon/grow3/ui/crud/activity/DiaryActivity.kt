package me.anon.grow3.ui.crud.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_crud_diary.*
import kotlinx.coroutines.launch
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.component
import me.anon.grow3.util.promptExit
import javax.inject.Inject

/**
 * Wizard activity for creating a new diary
 */
class DiaryActivity : BaseActivity(R.layout.activity_crud_diary)
{
	private var currentView = R.id.navigation_diary_details

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		component.inject(this)

		val navController = findNavController(R.id.nav_host_fragment)
		navController.addOnDestinationChangedListener { _, destination, _ ->
			currentView = destination.id
			back.isVisible = currentView != R.id.navigation_diary_details

			if (currentView == R.id.navigation_diary_complete)
			{
				next.isVisible = currentView != R.id.navigation_diary_complete
				back.isVisible = currentView != R.id.navigation_diary_complete
				toolbar?.isVisible = false
			}
		}
		NavigationUI.setupWithNavController(toolbar!!, navController)

		back.setOnClickListener {
			if (!navController.popBackStack())
			{
				promptExit {
					lifecycleScope.launch {
						viewModel.cancel()
						finish()
					}
				}
			}
		}

		next.setOnClickListener {
			when (currentView)
			{
				R.id.navigation_diary_details -> navController.navigate(R.id.page_1_to_2)
				R.id.navigation_diary_crops -> navController.navigate(R.id.page_2_to_3)
				R.id.navigation_diary_environment -> navController.navigate(R.id.page_3_to_4)
				R.id.navigation_diary_complete -> finish()
			}
		}
	}

	override fun onBackPressed()
	{
		back.performClick()
	}
}
