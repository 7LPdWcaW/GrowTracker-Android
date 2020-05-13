package me.anon.grow3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import me.anon.grow3.ui.diaries.DiariesListFragment

class MainActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)

		supportFragmentManager.commit {
			replace(R.id.nav_host_fragment, DiariesListFragment())
		}

//		val navController = findNavController(R.id.nav_host_fragment)
//		val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_gardens, R.id.navigation_schedules))
//
//		setupActionBarWithNavController(navController, appBarConfiguration)
//		nav_view.setupWithNavController(navController)
	}
}
