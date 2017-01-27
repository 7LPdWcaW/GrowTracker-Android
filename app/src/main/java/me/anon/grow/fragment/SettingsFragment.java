package me.anon.grow.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Base64;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.Unit;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.GsonHelper;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.DecryptTask;
import me.anon.lib.task.EncryptTask;
import me.anon.model.Garden;
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

		int defaultGardenIndex = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("default_garden", -1);
		String defaultGarden = defaultGardenIndex > -1 ? GardenManager.getInstance().getGardens().get(defaultGardenIndex).getName() : "All";
		findPreference("default_garden").setSummary(Html.fromHtml("Default garden to show on open, currently <b>" + defaultGarden + "</b>"));
		findPreference("delivery_unit").setSummary(Html.fromHtml("Default delivery measurement unit to use, currently <b>" + Unit.getSelectedDeliveryUnit(getActivity()).getLabel() + "</b>"));
		findPreference("measurement_unit").setSummary(Html.fromHtml("Default additive measurement unit to use, currently <b>" + Unit.getSelectedMeasurementUnit(getActivity()).getLabel() + "</b>"));

		try
		{
			findPreference("version").setTitle("Version " + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}

		findPreference("encrypt").setOnPreferenceChangeListener(this);
		findPreference("failsafe").setOnPreferenceChangeListener(this);
		findPreference("readme").setOnPreferenceClickListener(this);
		findPreference("export").setOnPreferenceClickListener(this);
		findPreference("default_garden").setOnPreferenceClickListener(this);
		findPreference("delivery_unit").setOnPreferenceClickListener(this);
		findPreference("measurement_unit").setOnPreferenceClickListener(this);

		findPreference("failsafe").setEnabled(((CheckBoxPreference)findPreference("encrypt")).isChecked());

		if (MainApplication.isFailsafe())
		{
			findPreference("failsafe").setTitle("");
			findPreference("failsafe").setSummary("");
			findPreference("encrypt").setTitle("");
			findPreference("encrypt").setSummary("");
		}
		else
		{
			populateAddons();
		}
	}

	/**
	 * Populates the preference category for installed addons
	 */
	private void populateAddons()
	{
		PreferenceCategory list = (PreferenceCategory)findPreference("addon_list");
		list.removeAll();

		Intent configIntents = new Intent("me.anon.grow.ADDON_CONFIGURATION");
		List<ResolveInfo> resolveInfos = getActivity().getPackageManager().queryIntentActivities(configIntents, PackageManager.GET_META_DATA);

		for (ResolveInfo resolveInfo : resolveInfos)
		{
			try
			{
				ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_META_DATA);
				Bundle bundle = ai.metaData;
				String name = bundle.getString("ADDON_NAME");
				String version = bundle.getString("ADDON_VERSION");

				Preference preference = new Preference(getActivity());
				preference.setIcon(resolveInfo.loadIcon(getActivity().getPackageManager()));
				preference.setTitle(name);
				preference.setSummary("Version: " + version + ". Tap to configure.");
				preference.setKey("activity://" + resolveInfo.activityInfo.name);
				list.addPreference(preference);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override public boolean onPreferenceChange(final Preference preference, Object newValue)
	{
		if ("encrypt".equals(preference.getKey()))
		{
			if ((Boolean)newValue == true)
			{
				new AlertDialog.Builder(getActivity())
					.setTitle("Warning")
					.setMessage("This is basic form of AES encryption based on a provided passphrase. This is not a guarantee form of encryption from law enforcement agencies as was designed to stop plain sight")
					.setPositiveButton("Accept", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
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
											if (plant != null && plant.getImages() != null)
											{
												new EncryptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, plant.getImages());
											}
										}

										ImageLoader.getInstance().clearMemoryCache();
										ImageLoader.getInstance().clearDiskCache();

										findPreference("failsafe").setEnabled(true);
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
					})
					.setNegativeButton("Decline", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							((CheckBoxPreference)preference).setChecked(false);
						}
					})
					.show();
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
							PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("encryption_check_key").apply();
							MainApplication.setEncrypted(false);
							PlantManager.getInstance().save();

							// Decrypt images
							for (Plant plant : PlantManager.getInstance().getPlants())
							{
								if (plant != null && plant.getImages() != null)
								{
									new DecryptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, plant.getImages());
								}
							}

							ImageLoader.getInstance().clearMemoryCache();
							ImageLoader.getInstance().clearDiskCache();
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
		else if ("failsafe".equals(preference.getKey()))
		{
			if ((Boolean)newValue == true)
			{
				new AlertDialog.Builder(getActivity())
					.setTitle("Warning")
					.setMessage("Provide this password during unencryption phase to prevent data from being loaded")
					.setPositiveButton("Accept", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
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
										PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
											.putString("failsafe_check_key", Base64.encodeToString(EncryptionHelper.encrypt(pin.toString(), pin.toString()), Base64.NO_WRAP))
											.apply();
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
					})
					.setNegativeButton("Decline", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							((CheckBoxPreference)preference).setChecked(false);
						}
					})
					.show();
			}
			else
			{
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
					.remove("failsafe_check_key")
					.apply();
			}

			return true;
		}

		return false;
	}

	@Override public boolean onPreferenceClick(Preference preference)
	{
		if ("delivery_unit".equals(preference.getKey()))
		{
			final String[] options = new String[Unit.values().length];
			int index = 0, selectedIndex = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("delivery_unit", 0);
			for (Unit unit : Unit.values())
			{
				options[index++] = unit.getLabel();
			}

			new AlertDialog.Builder(getActivity())
				.setTitle("Select measurement")
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("delivery_unit", index)
							.apply();

						findPreference("delivery_unit").setSummary(Html.fromHtml("Default delivery measurement unit to use, currently <b>" + Unit.getSelectedDeliveryUnit(getActivity()).getLabel() + "</b>"));
					}
				})
				.show();

			return true;
		}
		else if ("measurement_unit".equals(preference.getKey()))
		{
			final String[] options = new String[Unit.values().length];
			int index = 0, selectedIndex = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("measurement_unit", 0);
			for (Unit unit : Unit.values())
			{
				options[index++] = unit.getLabel();
			}

			new AlertDialog.Builder(getActivity())
				.setTitle("Select measurement")
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("measurement_unit", index)
							.apply();

					findPreference("measurement_unit").setSummary(Html.fromHtml("Default additive measurement unit to use, currently <b>" + Unit.getSelectedMeasurementUnit(getActivity()).getLabel() + "</b>"));
					}
				})
				.show();

			return true;
		}
		else if ("default_garden".equals(preference.getKey()))
		{
			final String[] options = new String[GardenManager.getInstance().getGardens().size() + 1];
			options[0] = "All";
			int index = 0, selectedIndex = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("default_garden", -1) + 1;
			for (Garden garden : GardenManager.getInstance().getGardens())
			{
				options[++index] = garden.getName();
			}

			new AlertDialog.Builder(getActivity())
				.setTitle("Select garden")
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("default_garden", index - 1)
							.apply();

						String defaultGarden = index - 1 > -1 ? GardenManager.getInstance().getGardens().get(index - 1).getName() : "All";
						findPreference("default_garden").setSummary(Html.fromHtml("Default garden to show on open, currently <b>" + defaultGarden + "</b>"));
					}
				})
				.show();

			return true;
		}
		else if ("readme".equals(preference.getKey()))
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
