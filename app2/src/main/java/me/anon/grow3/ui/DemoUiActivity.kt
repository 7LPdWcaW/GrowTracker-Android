package me.anon.grow3.ui

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_demo_ui.*
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseActivity

class DemoUiActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_demo_ui)
		setSupportActionBar(toolbar)
		title = "Demo UI"
	}
}
