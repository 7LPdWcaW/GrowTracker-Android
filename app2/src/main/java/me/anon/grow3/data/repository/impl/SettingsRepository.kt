package me.anon.grow3.data.repository.impl

import android.content.SharedPreferences
import me.anon.grow3.data.exceptions.GrowTrackerException
import me.anon.grow3.di.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
	@UserPrefs val prefs: SharedPreferences,
)
{
	public fun dateFormatType(): Int =
		when (val value = prefs.getString("date_precision", "precise")) {
			"precise" -> 0
			"simple" -> 1
			else -> throw GrowTrackerException.InvalidValue(value)
		}
}