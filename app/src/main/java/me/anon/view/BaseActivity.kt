package me.anon.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

/**
 * // TODO: Add class description
 */
open class BaseActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		val forceDark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("force_dark", false)
		AppCompatDelegate.setDefaultNightMode(if (forceDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

		super.onCreate(savedInstanceState)
	}
}
