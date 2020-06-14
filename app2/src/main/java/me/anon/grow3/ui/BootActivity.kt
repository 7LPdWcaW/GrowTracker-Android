package me.anon.grow3.ui

import android.os.Bundle
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.ui.setup.activity.SetupActivity
import me.anon.grow3.util.newTask

class BootActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val firstRun = false
		if (firstRun) newTask<SetupActivity>()
		else newTask<MainActivity>()

		finish()
	}
}
