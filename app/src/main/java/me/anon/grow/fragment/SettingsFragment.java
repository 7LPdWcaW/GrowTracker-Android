package me.anon.grow.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.Html;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import me.anon.grow.R;
import me.anon.lib.helper.GsonHelper;
import me.anon.lib.manager.PlantManager;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		try
		{
			findPreference("version").setSummary("Version " + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}

		findPreference("encrypt").setOnPreferenceChangeListener(this);
		findPreference("readme").setOnPreferenceClickListener(this);
		findPreference("export").setOnPreferenceClickListener(this);
	}

	@Override public boolean onPreferenceChange(final Preference preference, Object newValue)
	{
		if ("encrypt".equals(preference.getKey()))
		{
			if ((Boolean)newValue == true)
			{
				final StringBuffer pin = new StringBuffer();
				final PinDialogFragment check1 = new PinDialogFragment();
				final PinDialogFragment check2 = new PinDialogFragment();

				check1.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
				{
					@Override public void onDialogConfirmed(String input)
					{
						pin.append(input);
						check2.show(getFragmentManager(), null);
					}
				});

				check2.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
				{
					@Override public void onDialogConfirmed(String input)
					{
						if (input.equals(pin.toString()))
						{
							// encrypt
						}
						else
						{
							((CheckBoxPreference)preference).setChecked(false);
						}
					}
				});

				check1.show(getFragmentManager(), null);
			}

			return true;
		}

		return false;
	}

	@Override public boolean onPreferenceClick(Preference preference)
	{
		if ("readme".equals(preference.getKey()))
		{
			String readme = "";

			try
			{
				InputStream stream = new BufferedInputStream(getActivity().getAssets().open("readme.html"), 8192);
				int len = 0;
				byte[] buffer = new byte[8192];

				while ((len = stream.read(buffer)) != -1)
				{
					readme += new String(buffer, 0, len);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			new AlertDialog.Builder(getActivity())
				.setMessage(Html.fromHtml(readme))
				.show();

			return true;
		}
		else if ("export".equals(preference.getKey()))
		{
			String json = GsonHelper.parse(PlantManager.getInstance().getPlants());

			Intent share = new Intent(Intent.ACTION_SEND);
			share.putExtra(Intent.EXTRA_TEXT, json);
			share.setType("text/plain");
			startActivity(share);

			return true;
		}

		return false;
	}
}
