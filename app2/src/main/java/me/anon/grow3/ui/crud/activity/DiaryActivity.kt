package me.anon.grow3.ui.crud.activity

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import kotlinx.android.synthetic.main.activity_crud_diary.*
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseActivity

class DiaryActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_crud_diary)

		val navController = findNavController(R.id.nav_host_fragment)
		setupWithNavController(toolbar, navController)
	}
}
