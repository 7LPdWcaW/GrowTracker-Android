package me.anon.grow

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_holder.toolbar
import kotlinx.android.synthetic.main.fragment_holder.toolbar_layout
import kotlinx.android.synthetic.main.tabbed_fragment_holder.*
import me.anon.grow.fragment.ActionsListFragment
import me.anon.grow.fragment.PlantDetailsFragment
import me.anon.grow.fragment.StatisticsFragment
import me.anon.grow.fragment.ViewPhotosFragment
import me.anon.model.Plant

class PlantDetailsActivity : BaseActivity()
{
	public val toolbarLayout: AppBarLayout by lazy { toolbar_layout }
	private lateinit var plant: Plant

	override fun onCreate(savedInstanceState: Bundle?)
	{
		if (!checkEncryptState())
		{
			super.onCreate(savedInstanceState)

			setContentView(R.layout.tabbed_fragment_holder)
			setSupportActionBar(toolbar)
			supportActionBar?.setDisplayHomeAsUpEnabled(true)
			supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp)

			supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) ?: let {
				val fragment = when (intent.extras?.get("forward"))
				{
					"photos" -> {
						tabs.selectedItemId = R.id.view_photos
						ViewPhotosFragment.newInstance(intent.extras)
					}
					"events" -> {
						tabs.selectedItemId = R.id.view_history
						ActionsListFragment.newInstance(intent.extras)
					}
					"statistics" -> {
						tabs.selectedItemId = R.id.view_statistics
						StatisticsFragment.newInstance(intent.extras)
					}
					else -> PlantDetailsFragment.newInstance(intent.extras)
				}
				supportFragmentManager.beginTransaction().replace(R.id.coordinator, fragment, TAG_FRAGMENT).commit()
			}

			tabs.visibility = View.GONE
			intent.extras?.get("plant")?.let {
				plant = it as Plant

				tabs.visibility = View.VISIBLE
				tabs.setOnNavigationItemSelectedListener {
					plant = intent.extras?.get("plant") as Plant
					val fragment = supportFragmentManager.findFragmentById(R.id.coordinator)

					if (fragment is PlantDetailsFragment)
					{
						fragment.save()
					}

					toolbarLayout.removeViews(1, toolbarLayout.childCount - 1)
					supportFragmentManager.beginTransaction()
						.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
						.replace(R.id.coordinator, when (it.itemId)
						{
							R.id.view_details -> PlantDetailsFragment.newInstance(intent.extras)
							R.id.view_history -> ActionsListFragment.newInstance(intent.extras)
							R.id.view_photos -> ViewPhotosFragment.newInstance(intent.extras)
							R.id.view_statistics -> StatisticsFragment.newInstance(intent.extras)
							else -> Fragment()
						}, TAG_FRAGMENT)
						.commit()

					return@setOnNavigationItemSelectedListener true
				}
			}
		}
	}

	override fun onNewIntent(intent: Intent?)
	{
		super.onNewIntent(intent)
		intent?.extras?.get("plant")?.let {
			plant = it as Plant
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		if (item.itemId == android.R.id.home)
		{
			val fragment = supportFragmentManager.findFragmentById(R.id.coordinator)

			if (fragment is PlantDetailsFragment)
			{
				fragment.save()
			}

			finish()

			return true
		}

		return super.onOptionsItemSelected(item)
	}

	companion object
	{
		private val TAG_FRAGMENT = "current_fragment"
	}
}
