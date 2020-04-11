package me.anon.view

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.tabbed_fragment_holder.*
import me.anon.grow.R
import me.anon.view.fragment.PlantDetailsFragment

/**
 * // TODO: Add class description
 */
class PlantDetailsActivity2 : BaseActivity()
{
	companion object
	{
		public const val TAG_FRAGMENT = "fragment"
	}

	class LocalViewModel(
		private val savedStateHandle: SavedStateHandle
	) : ViewModel()
	{
		public var plantId: String? = null
			set(value)
			{
				field = value
				savedStateHandle["plantId"] = plantId
			}

		init {
			plantId = savedStateHandle["plantId"] ?: plantId
		}
	}

	private val viewModel: LocalViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		intent.extras?.let {
			viewModel.plantId = it.getString("plantId")
		}

		setContentView(R.layout.tabbed_fragment_holder)
		setupToolbar(toolbar)
		setupUi()
	}

	private fun setupToolbar(toolbar: MaterialToolbar)
	{
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp)
	}

	private fun setupUi()
	{
		tabs.visibility = View.GONE
		viewModel.plantId?.let { plantId ->
			tabs.visibility = View.VISIBLE
			tabs.setOnNavigationItemSelectedListener {
				supportFragmentManager.beginTransaction()
					.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
					.replace(R.id.fragment_holder, when (it.itemId)
					{
						R.id.view_details -> PlantDetailsFragment.newInstance(plantId)
//						R.id.view_history -> ActionsListFragment.newInstance(intent.extras)
//						R.id.view_photos -> ViewPhotosFragment.newInstance(intent.extras)
//						R.id.view_statistics -> StatisticsFragment.newInstance(intent.extras)
						else -> Fragment()
					}, TAG_FRAGMENT)
					.commit()

				return@setOnNavigationItemSelectedListener true
			}
		}

		supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) ?: let {
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_holder, PlantDetailsFragment.newInstance(viewModel.plantId), TAG_FRAGMENT)
				.commit()
		}
	}
}
