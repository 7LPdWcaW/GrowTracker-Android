package me.anon.grow3.data.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CorePreferences @Inject constructor(
	@Named("core_prefs") val prefs: SharedPreferences
)
{
	public var isFirstLaunch: Boolean
		get() = prefs.getBoolean("first_launch", true)
		set(value) {
			prefs.edit {
				putBoolean("first_launch", value)
			}
		}
}
