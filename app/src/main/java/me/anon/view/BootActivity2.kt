package me.anon.view

import android.content.Intent
import android.os.Bundle

/**
 * // TODO: Add class description
 */
class BootActivity2 : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val main = Intent(this, MainActivity2::class.java)
		startActivity(main)
		finish()
	}
}
