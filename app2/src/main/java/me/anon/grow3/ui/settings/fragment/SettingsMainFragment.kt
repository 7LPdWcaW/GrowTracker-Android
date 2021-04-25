package me.anon.grow3.ui.settings.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.anon.grow3.R

class SettingsMainFragment : PreferenceFragmentCompat()
{
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		setPreferencesFromResource(R.xml.main_preferences, rootKey)
	}
}
