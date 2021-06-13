package me.anon.grow3.ui

import android.os.Bundle
import me.anon.grow3.data.preferences.CorePreferences
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.ui.setup.activity.SetupActivity
import me.anon.grow3.util.component
import me.anon.grow3.util.newTask
import javax.inject.Inject

class BootActivity : BaseActivity()
{
	@Inject internal lateinit var prefs: CorePreferences

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		component.inject(this)

		if (prefs.isFirstLaunch) newTask<SetupActivity>()
		else newTask<MainActivity>()

		finish()
	}
}
