package me.anon.grow3.data.preferences

import android.content.Context
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CorePreferences @Inject constructor(val context: Context)
{
	private val sharedPrefs = context.getSharedPreferences("core_prefs", Context.MODE_PRIVATE)

	public fun isFirstLaunch(): Boolean = sharedPrefs.getBoolean("first_launch", true)
	public fun completeFirstLaunch()
	{
		sharedPrefs.edit {
			putBoolean("first_launch", false)
		}
	}
}
