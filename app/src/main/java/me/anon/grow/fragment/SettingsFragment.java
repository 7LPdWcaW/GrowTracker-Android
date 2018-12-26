package me.anon.grow.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import me.anon.controller.receiver.BackupService;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.TempUnit;
import me.anon.lib.Unit;
import me.anon.lib.helper.AddonHelper;
import me.anon.lib.helper.BackupHelper;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.PathHelper;
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
	private static final int REQUEST_UNINSTALL = 0x01;
	private static final int REQUEST_PICK_DOCUMENT = 0x02;

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		int defaultGardenIndex = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("default_garden", -1);
		String defaultGarden = defaultGardenIndex > -1 ? GardenManager.getInstance().getGardens().get(defaultGardenIndex).getName() : "All";
		findPreference("default_garden").setSummary(Html.fromHtml("Default garden to show on open, currently <b>" + defaultGarden + "</b>"));
		findPreference("delivery_unit").setSummary(Html.fromHtml("Default delivery measurement unit to use, currently <b>" + Unit.getSelectedDeliveryUnit(getActivity()).getLabel() + "</b>"));
		findPreference("measurement_unit").setSummary(Html.fromHtml("Default additive measurement unit to use, currently <b>" + Unit.getSelectedMeasurementUnit(getActivity()).getLabel() + "</b>"));
		findPreference("temperature_unit").setSummary(Html.fromHtml("Default temperature unit to use, currently <b>" + TempUnit.getSelectedTemperatureUnit(getActivity()).getLabel() + "</b>"));
		findPreference("image_location").setSummary(Html.fromHtml("Image storage location, currently <b>" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/</b>"));

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
		findPreference("auto_backup").setOnPreferenceChangeListener(this);
		findPreference("readme").setOnPreferenceClickListener(this);
		findPreference("export").setOnPreferenceClickListener(this);
		findPreference("default_garden").setOnPreferenceClickListener(this);
		findPreference("delivery_unit").setOnPreferenceClickListener(this);
		findPreference("measurement_unit").setOnPreferenceClickListener(this);
		findPreference("temperature_unit").setOnPreferenceClickListener(this);
		findPreference("backup_now").setOnPreferenceClickListener(this);
		findPreference("image_location").setOnPreferenceClickListener(this);

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
		final PackageManager packageManager = getActivity().getPackageManager();
		final PreferenceCategory list = (PreferenceCategory)findPreference("addon_list");
		list.removeAll();

		for (final String addonAction : AddonHelper.ADDON_BROADCAST)
		{
			Intent otherIntents = new Intent(addonAction);
			List<ResolveInfo> resolveInfos = packageManager.queryBroadcastReceivers(otherIntents, PackageManager.GET_META_DATA);

			if (resolveInfos.size() > 0)
			{
				for (final ResolveInfo resolveInfo : resolveInfos)
				{
					try
					{
						String appName = (String)resolveInfo.loadLabel(packageManager);
						appName = TextUtils.isEmpty(appName) ? resolveInfo.activityInfo.packageName : appName;

						ApplicationInfo ai = packageManager.getApplicationInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_META_DATA);
						Bundle bundle = ai.metaData;
						final String name = bundle.getString("me.anon.grow.ADDON_NAME", appName);
						final String version = String.valueOf(bundle.get("me.anon.grow.ADDON_VERSION"));

						Preference preference = new Preference(getActivity());

						Drawable icon = resolveInfo.loadIcon(packageManager);
						if (icon == null)
						{
							icon = getResources().getDrawable(R.drawable.ic_configure);
						}

						preference.setIcon(icon);
						preference.setTitle(name + " " + version);
						preference.setSummary("Tap for more information");
						preference.setKey(resolveInfo.activityInfo.packageName);
						preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
						{
							@Override public boolean onPreferenceClick(Preference preference)
							{
								Intent configIntent = new Intent(addonAction);
								configIntent.setPackage(preference.getKey());
								configIntent.addCategory("me.anon.grow.ADDON_CONFIGURATION");

								final List<ResolveInfo> configureIntents = packageManager.queryIntentActivities(configIntent, PackageManager.GET_META_DATA);

								final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
									.setTitle(name + " " + version)
									.setMessage(Html.fromHtml(AddonHelper.ADDON_DESCRIPTIONS.get(addonAction)))
									.setNeutralButton("Configure", new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which)
										{
											Intent intent = new Intent();
											intent.setComponent(new ComponentName(configureIntents.get(0).activityInfo.packageName, configureIntents.get(0).activityInfo.name));
											startActivity(intent);
										}
									})
									.setPositiveButton("Close", null)
									.setNegativeButton("Uninstall", new DialogInterface.OnClickListener()
									{
										@Override public void onClick(DialogInterface dialog, int which)
										{
											Intent intent = new Intent(Intent.ACTION_DELETE);
											intent.setData(Uri.parse("package:" + resolveInfo.activityInfo.packageName));
											intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
											startActivityForResult(intent, REQUEST_UNINSTALL);
										}
									}).create();

								if (configureIntents.size() == 0)
								{
									alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
									{
										@Override public void onShow(DialogInterface dialog)
										{
											alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);
										}
									});
								}

								alertDialog.show();

								return true;
							}
						});
						list.addPreference(preference);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		if (list.getPreferenceCount() == 0)
		{
			getPreferenceScreen().removePreference(list);
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
									if (pin.toString().equals(String.valueOf(input)))
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
					.setOnDismissListener(new DialogInterface.OnDismissListener()
					{
						@Override public void onDismiss(DialogInterface dialog)
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
		else if ("auto_backup".equalsIgnoreCase(preference.getKey()))
		{
			if ((Boolean)newValue)
			{
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("auto_backup", true).apply();
				((MainApplication)getActivity().getApplication()).registerBackupService();
				Toast.makeText(getActivity(), "Backup enabled, backups will be stored in /sdcard/backups/GrowTracker/", Toast.LENGTH_LONG).show();
			}
			else
			{
				Intent backupIntent = new Intent(getActivity(), BackupService.class);
				AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
				alarmManager.cancel(PendingIntent.getBroadcast(getActivity(), 0, backupIntent, 0));
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
			int index = 0, selectedIndex = Unit.getSelectedDeliveryUnit(getActivity()).ordinal();
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
			int index = 0, selectedIndex = Unit.getSelectedMeasurementUnit(getActivity()).ordinal();
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
		else if ("temperature_unit".equals(preference.getKey()))
		{
			final String[] options = new String[TempUnit.values().length];
			int index = 0, selectedIndex = TempUnit.getSelectedTemperatureUnit(getActivity()).ordinal();
			for (TempUnit unit : TempUnit.values())
			{
				options[index++] = unit.getLabel();
			}

			new AlertDialog.Builder(getActivity())
				.setTitle("Select temperature unit")
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("temperature_unit", index)
							.apply();

					findPreference("temperature_unit").setSummary(Html.fromHtml("Default temperature unit to use, currently <b>" + TempUnit.getSelectedTemperatureUnit(getActivity()).getLabel() + "</b>"));
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
			Uri contentUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", new File(PlantManager.FILES_DIR, "plants.json"));

			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			shareIntent.setDataAndType(contentUri, getActivity().getContentResolver().getType(contentUri));
			shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
			startActivity(Intent.createChooser(shareIntent, "Share with"));

			return true;
		}
		else if ("backup_now".equals(preference.getKey()))
		{
			Toast.makeText(getActivity(), "Backed up to " + BackupHelper.backupJson().getPath(), Toast.LENGTH_SHORT).show();
		}
		else if ("image_location".equals(preference.getKey()))
		{
			if (Build.VERSION.SDK_INT >= 21)
			{
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				startActivityForResult(intent, REQUEST_PICK_DOCUMENT);
			}
		}

		return false;
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_UNINSTALL)
		{
			// refresh addons
			Toast.makeText(getActivity(), "Addon successfully uninstalled", Toast.LENGTH_SHORT).show();
			populateAddons();
		}
		else if (requestCode == REQUEST_PICK_DOCUMENT && Build.VERSION.SDK_INT >= 19)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Uri treeUri = data.getData();
				DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);

				if (pickedDir != null)
				{
					String filePath;

					if (!TextUtils.isEmpty(filePath))
					{
						findPreference("image_location").getEditor().putString("image_location", filePath).apply();
						findPreference("image_location").setSummary(Html.fromHtml("Image storage location, currently <b>" + filePath + "</b>"));
					}

					return;
				}
			}

			Toast.makeText(getActivity(), "There was a problem with the selected location", Toast.LENGTH_SHORT).show();
		}
	}
}
