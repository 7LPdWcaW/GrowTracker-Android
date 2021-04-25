package me.anon.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.main_view.*
import me.anon.grow.R
import me.anon.grow.SettingsActivity
import me.anon.view.fragment.PlantListFragment
import me.anon.view.viewmodel.MainViewModel
import me.anon.view.viewmodel.ViewModelFactory

/**
 * // TODO: Add class description
 */
class MainActivity2 : BaseActivity(), NavigationView.OnNavigationItemSelectedListener
{
	private val viewModel: MainViewModel by viewModels { ViewModelFactory(application as MainApplication2, this) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)
//		setSupportActionBar(toolbar)
//		setupDrawerToggle()
//		setupNavigation()
//		viewModel.start()
	}

	private fun setupDrawerToggle()
	{
		drawer_layout?.let { drawer ->
			val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(this, drawer, toolbar, 0, 0)
			{
				override fun onDrawerSlide(drawerView: View, slideOffset: Float)
				{
					super.onDrawerSlide(drawerView, slideOffset)
					if (currentFocus != null)
					{
						val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
						inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
					}
				}
			}
			drawer.addDrawerListener(drawerToggle)
			drawerToggle.syncState()
		}
	}

	private fun setupNavigation()
	{
		viewModel.gardens.observe(this) { gardens ->
			navigation_view.menu.clear()
			navigation_view.setNavigationItemSelectedListener(this)
			navigation_view.inflateMenu(R.menu.navigation_drawer)

			with (navigation_view) {
				var index = 0
				gardens.forEach { garden ->
					menu.findItem(R.id.garden_menu)
						.subMenu
						.add(R.id.garden_menu, 100 or index++, 1, garden.name)
						.isCheckable = true
				}

				menu.findItem(R.id.version)
					.setTitle(getString(R.string.version, packageManager.getPackageInfo(packageName, 0).versionName))
			}
		}

		viewModel.selectedPage.observe(this) { pageId ->
			if ((pageId and 100) == 100)
			{
				val position = pageId xor 100
				navigation_view.menu.findItem(R.id.all).isChecked = false
			}
			else
			{
				when (pageId)
				{
					R.id.all -> selectFragment(PlantListFragment())

					R.id.settings -> {
						val settings = Intent(this, SettingsActivity::class.java)
						startActivityForResult(settings, 5)
					}
				}
			}

			navigation_view.menu.findItem(pageId).isChecked = true
		}
	}

	private fun selectFragment(fragment: Fragment)
	{
		var current: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_holder)
		if (current == null || !fragment::class.java.isAssignableFrom(current::class.java))
		{
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_holder, fragment)
				.commit()
		}
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean
	{
		viewModel.setSelectedPage(item.itemId)
		return true
	}
}


