package me.anon.grow.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.GsonHelper;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.DecryptTask;
import me.anon.lib.task.EncryptTask;
import me.anon.model.Plant;

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

				check1.setTitle("Enter a passphrase");
				check1.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
				{
					@Override public void onDialogConfirmed(String input)
					{
						pin.append(input);
						check2.show(getFragmentManager(), null);
					}
				});

				check2.setTitle("Re-enter your passphrase");
				check2.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
				{
					@Override public void onDialogConfirmed(String input)
					{
						if (input.equals(pin.toString()))
						{
							// Encrypt plant data
							PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
								.putString("encryption_check_key", Base64.encodeToString(EncryptionHelper.encrypt(pin.toString(), pin.toString()), Base64.NO_WRAP))
								.apply();

							MainApplication.setEncrypted(true);
							MainApplication.setKey(pin.toString());
							PlantManager.getInstance().save();

							// Encrypt images
							for (Plant plant : PlantManager.getInstance().getPlants())
							{
								new EncryptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, plant.getImages());
							}
						}
						else
						{
							((CheckBoxPreference)preference).setChecked(false);
							Toast.makeText(getActivity(), "Error - passphrases did not match up", Toast.LENGTH_SHORT).show();
						}
					}
				});

				check1.show(getFragmentManager(), null);
			}
			else
			{
				final PinDialogFragment check = new PinDialogFragment();
				check.setTitle("Enter your passphrase");
				check.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
				{
					@Override public void onDialogConfirmed(String input)
					{
						String check = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("encryption_check_key", "");
						String inputCheck = Base64.encodeToString(EncryptionHelper.encrypt(input, input), Base64.NO_WRAP);

						if (inputCheck.equals(check))
						{
							// Decrypt plant data
							PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
								.remove("encryption_check_key")
								.apply();
							MainApplication.setEncrypted(false);
							PlantManager.getInstance().save();

							// Decrypt images
							for (Plant plant : PlantManager.getInstance().getPlants())
							{
								new DecryptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, plant.getImages());
							}
						}
						else
						{
							((CheckBoxPreference)preference).setChecked(true);
							Toast.makeText(getActivity(), "Error - incorrect passphrase", Toast.LENGTH_SHORT).show();
						}
					}
				});

				check.show(getFragmentManager(), null);
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
