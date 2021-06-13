package me.anon.grow3.data.preferences

import android.content.Context
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CorePreferences @Inject constructor(val context: Context)
{
	private val sharedPrefs = context.getSharedPreferences("core_prefs", Context.MODE_PRIVATE)

	public var isFirstLaunch: Boolean
		get() = sharedPrefs.getBoolean("first_launch", true)
		set(value) {
			sharedPrefs.edit {
				putBoolean("first_launch", value)
			}
		}
}
