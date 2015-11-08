package me.anon.grow.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import me.anon.grow.R;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class SettingsFragment extends PreferenceFragment
{
	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}
}
