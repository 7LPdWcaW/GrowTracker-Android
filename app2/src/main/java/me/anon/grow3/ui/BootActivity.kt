package me.anon.grow3.ui

import android.os.Bundle
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.setup.activity.SetupActivity
import me.anon.grow3.util.navigateTo

class BootActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val firstRun = false
		if (firstRun) navigateTo<SetupActivity>()
		else navigateTo<MainActivity>()

		finish()
	}
}
