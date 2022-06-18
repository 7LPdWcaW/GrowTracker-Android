package me.anon.grow.fragment;

import android.app.AlarmManager;
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
import android.provider.DocumentsContract;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.moshi.Types;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import me.anon.controller.receiver.BackupService;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.SnackBar;
import me.anon.lib.TdsUnit;
import me.anon.lib.TempUnit;
import me.anon.lib.Unit;
import me.anon.lib.helper.AddonHelper;
import me.anon.lib.helper.BackupHelper;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.MigrationHelper;
import me.anon.lib.helper.MoshiHelper;
import me.anon.lib.helper.NotificationHelper;
import me.anon.lib.helper.PathHelper;
import me.anon.lib.manager.FileManager;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.manager.ScheduleManager;
import me.anon.lib.task.DecryptTask;
import me.anon.lib.task.EncryptTask;
import me.anon.model.Garden;
import me.anon.model.Plant;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	private static final int REQUEST_UNINSTALL = 0x01;
	private static final int REQUEST_PICK_DOCUMENT = 0x02;
	private static final int REQUEST_PICK_BACKUP_DOCUMENT = 0x03;
	private static final int REQUEST_PICK_IMPORT_DOCUMENT = 0x04;

	@Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		setPreferencesFromResource(R.xml.preferences, rootKey);

		int defaultGardenIndex = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("default_garden", -1);
		String defaultGarden = getString(R.string.all);

		if (defaultGardenIndex > -1 && defaultGardenIndex < GardenManager.getInstance().getGardens().size())
		{
			try
			{
				defaultGarden = GardenManager.getInstance().getGardens().get(defaultGardenIndex).getName();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		findPreference("default_garden").setSummary(Html.fromHtml(getString(R.string.settings_default_garden, defaultGarden)));

		findPreference("delivery_unit").setSummary(Html.fromHtml(getString(R.string.settings_delivery, Unit.getSelectedDeliveryUnit(getActivity()).getLabel())));
		findPreference("measurement_unit").setSummary(Html.fromHtml(getString(R.string.settings_measurement, Unit.getSelectedMeasurementUnit(getActivity()).getLabel())));
		findPreference("temperature_unit").setSummary(Html.fromHtml(getString(R.string.settings_temperature, TempUnit.getSelectedTemperatureUnit(getActivity()).getLabel())));
		findPreference("tds_unit").setSummary(Html.fromHtml(getString(R.string.settings_tds_summary, getString(TdsUnit.getSelectedTdsUnit(getActivity()).getStrRes()))));
		findPreference("backup_now").setSummary(Html.fromHtml(getString(R.string.settings_lastbackup_summary, BackupHelper.getLastBackup())));
		findPreference("image_location").setSummary(Html.fromHtml(getString(R.string.settings_image_location_summary, FileManager.IMAGE_PATH)));
		findPreference("backup_location").setSummary(Html.fromHtml(getString(R.string.settings_backup_location_summary, BackupHelper.FILES_PATH)));

		try
		{
			findPreference("version").setTitle("Version " + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}

		findPreference("encrypt").setOnPreferenceChangeListener(this);
		findPreference("encrypt").setEnabled(!MainApplication.dataTaskRunning.get());
		((SwitchPreferenceCompat)findPreference("encrypt")).setChecked(MainApplication.isEncrypted());

		findPreference("failsafe").setOnPreferenceChangeListener(this);
		findPreference("auto_backup").setOnPreferenceChangeListener(this);
		findPreference("backup_size").setOnPreferenceChangeListener(this);
		findPreference("force_dark").setOnPreferenceChangeListener(this);
		String currentBackup = findPreference("backup_size").getSharedPreferences().getString("backup_size", "20");

		if (Build.VERSION.SDK_INT >= 21)
		{
			findPreference("backup_size").setSummary(Html.fromHtml(getString(R.string.settings_backup_size, currentBackup, lengthToString(BackupHelper.backupSize()))));
		}
		else
		{
			findPreference("image_location").setEnabled(false);
			findPreference("backup_location").setEnabled(false);
			findPreference("backup_size").setEnabled(false);
		}

		findPreference("readme").setOnPreferenceClickListener(this);
		findPreference("clear_image_cache").setOnPreferenceClickListener(this);
		findPreference("export").setOnPreferenceClickListener(this);
		findPreference("default_garden").setOnPreferenceClickListener(this);
		findPreference("delivery_unit").setOnPreferenceClickListener(this);
		findPreference("measurement_unit").setOnPreferenceClickListener(this);
		findPreference("temperature_unit").setOnPreferenceClickListener(this);
		findPreference("tds_unit").setOnPreferenceClickListener(this);
		findPreference("backup_now").setOnPreferenceClickListener(this);
		findPreference("restore").setOnPreferenceClickListener(this);
		findPreference("image_location").setOnPreferenceClickListener(this);
		findPreference("backup_location").setOnPreferenceClickListener(this);
		findPreference("import").setOnPreferenceClickListener(this);

		findPreference("import").setEnabled(!MainApplication.isEncrypted());
		findPreference("failsafe").setEnabled(((SwitchPreferenceCompat)findPreference("encrypt")).isChecked());

		if (MainApplication.isFailsafe())
		{
			findPreference("failsafe").setTitle("Redacted");
			findPreference("failsafe").setSummary("Redacted");
			findPreference("encrypt").setTitle("Redacted");
			findPreference("encrypt").setSummary("Redacted");
		}
		else
		{
			populateAddons();
		}

		if (MainApplication.isPanic)
		{
			getPreferenceScreen().removePreference(findPreference("settings_general"));
			getPreferenceScreen().removePreference(findPreference("settings_units"));
			getPreferenceScreen().removePreference(findPreference("addon_list"));
			getPreferenceScreen().removePreference(findPreference("failsafe"));
			getPreferenceScreen().removePreference(findPreference("encrypt"));
			((PreferenceGroup)getPreferenceScreen().findPreference("settings_data")).notifyDependencyChange(false);
		}
		else
		{
			Intent refresh = new Intent();
			refresh.putExtra("refresh", true);
			getActivity().setResult(RESULT_OK, refresh);
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
		Intent refresh = new Intent();
		refresh.putExtra("refresh", true);
		getActivity().setResult(RESULT_OK, refresh);

		if ("force_dark".equals(preference.getKey()))
		{
			PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("force_dark", (boolean)newValue).apply();
			AppCompatDelegate.setDefaultNightMode((boolean)newValue ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
			getActivity().recreate();
		}
		else if ("backup_size".equals(preference.getKey()))
		{
			String currentBackup = (String)newValue;
			PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
				.putString("backup_size", currentBackup)
				.apply();
			((EditTextPreference)preference).setText(currentBackup);
			BackupHelper.limitBackups(currentBackup);
			findPreference("backup_size").setSummary(Html.fromHtml(getString(R.string.settings_backup_size, currentBackup, lengthToString(BackupHelper.backupSize()))));
		}
		else if ("encrypt".equals(preference.getKey()))
		{
			NotificationHelper.createExportChannel(getActivity());

			if ((Boolean)newValue == true)
			{
				new AlertDialog.Builder(getActivity())
					.setTitle(R.string.warning)
					.setMessage(R.string.encryption_message)
					.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							final StringBuffer pin = new StringBuffer();
							final PinDialogFragment check1 = new PinDialogFragment();
							final PinDialogFragment check2 = new PinDialogFragment();

							check1.setTitle(getString(R.string.add_passphrase_title));
							check1.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
							{
								@Override public void onDialogConfirmed(DialogInterface dialog, String input)
								{
									dialog.dismiss();
									pin.append(input);
									check2.show(((FragmentActivity)getActivity()).getSupportFragmentManager(), null);
								}
							});
							check1.setOnDialogCancelled(new PinDialogFragment.OnDialogCancelled()
							{
								@Override public void onDialogCancelled()
								{
									// make sure the preferences is definitely turned off
									((SwitchPreferenceCompat)preference).setChecked(false);
								}
							});

							check2.setTitle(getString(R.string.readd_passphrase_title));
							check2.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
							{
								@Override public void onDialogConfirmed(DialogInterface dialog, String input)
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
										ArrayList<String> images = new ArrayList<>();
										for (Plant plant : PlantManager.getInstance().getPlants())
										{
											if (plant != null && plant.getImages() != null)
											{
												images.addAll(plant.getImages());
											}
										}

										NotificationHelper.sendDataTaskNotification(getActivity(), getString(R.string.app_name), getString(R.string.encrypt_progress_warning));
										new EncryptTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, images);
										ImageLoader.getInstance().clearMemoryCache();
										ImageLoader.getInstance().clearDiskCache();
										FileManager.getInstance().removeFile(PlantManager.FILES_DIR + "/plants.json");
										FileManager.getInstance().removeFile(PlantManager.FILES_DIR + "/plants.json.bak");
										FileManager.getInstance().removeFile(PlantManager.FILES_DIR + "/plants.temp");

										SnackBar.show(getActivity(), getString(R.string.encrypt_progress_warning), Snackbar.LENGTH_LONG, null);

										// make sure encrypt mode is definitely enabled
										((SwitchPreferenceCompat)preference).setChecked(true);
										((SwitchPreferenceCompat)preference).setEnabled(false);
										findPreference("failsafe").setEnabled(true);
										dialog.dismiss();
									}
									else
									{
										((SwitchPreferenceCompat)preference).setChecked(false);
										((SwitchPreferenceCompat)preference).setEnabled(true);
										check2.getInput().setError(getString(R.string.passphrase_error));
									}
								}
							});
							check2.setOnDialogCancelled(new PinDialogFragment.OnDialogCancelled()
							{
								@Override public void onDialogCancelled()
								{
									// make sure the preferences is definitely turned off
									((SwitchPreferenceCompat)preference).setChecked(false);
									((SwitchPreferenceCompat)preference).setEnabled(true);
								}
							});

							check1.show(((FragmentActivity)getActivity()).getSupportFragmentManager(), null);
						}
					})
					.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							((SwitchPreferenceCompat)preference).setChecked(false);
						}
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener()
					{
						@Override public void onCancel(DialogInterface dialog)
						{
							((SwitchPreferenceCompat)preference).setChecked(false);
						}
					})
					.show();
			}
			else
			{
				final PinDialogFragment check = new PinDialogFragment();
				check.setTitle(getString(R.string.passphrase_title));
				check.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
				{
					@Override public void onDialogConfirmed(DialogInterface dialog, String input)
					{
						String checkStr = android.preference.PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("encryption_check_key", Base64.encodeToString(EncryptionHelper.encrypt(input, input), Base64.NO_WRAP));
						String inputCheck = Base64.encodeToString(EncryptionHelper.encrypt(input, input), Base64.NO_WRAP);

						if (inputCheck.equals(checkStr))
						{
							// Decrypt plant data
							PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("encryption_check_key").apply();
							MainApplication.setEncrypted(false);
							PlantManager.getInstance().save();

							// Decrypt images
							ArrayList<String> images = new ArrayList<>();
							for (Plant plant : PlantManager.getInstance().getPlants())
							{
								if (plant != null && plant.getImages() != null)
								{
									images.addAll(plant.getImages());
								}
							}

							NotificationHelper.sendDataTaskNotification(getActivity(), getString(R.string.app_name), getString(R.string.decrypt_progress_warning));
							new DecryptTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, images);

							SnackBar.show(getActivity(), getString(R.string.decrypt_progress_warning), Snackbar.LENGTH_LONG, null);

							// make sure the preferences is definitely turned off
							((SwitchPreferenceCompat)preference).setChecked(false);
							((SwitchPreferenceCompat)preference).setEnabled(false);
							ImageLoader.getInstance().clearMemoryCache();
							ImageLoader.getInstance().clearDiskCache();
							dialog.dismiss();
						}
						else
						{
							((SwitchPreferenceCompat)preference).setChecked(true);
							((SwitchPreferenceCompat)preference).setEnabled(true);
							check.getInput().setError(getString(R.string.passphrase_error));
						}
					}
				});
				check.setOnDialogCancelled(new PinDialogFragment.OnDialogCancelled()
				{
					@Override public void onDialogCancelled()
					{
						((SwitchPreferenceCompat)preference).setChecked(true);
						((SwitchPreferenceCompat)preference).setEnabled(true);
					}
				});

				check.show(((FragmentActivity)getActivity()).getSupportFragmentManager(), null);
			}

			return true;
		}
		else if ("failsafe".equals(preference.getKey()))
		{
			if ((Boolean)newValue == true)
			{
				new AlertDialog.Builder(getActivity())
					.setTitle(R.string.warning)
					.setMessage(R.string.failsafe_message)
					.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							final StringBuffer pin = new StringBuffer();
							final PinDialogFragment check1 = new PinDialogFragment();
							final PinDialogFragment check2 = new PinDialogFragment();

							check1.setTitle(getString(R.string.passphrase_title));
							check1.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
							{
								@Override public void onDialogConfirmed(DialogInterface dialog, String input)
								{
									pin.append(input);
									check2.show(((FragmentActivity)getActivity()).getSupportFragmentManager(), null);
								}
							});

							check2.setTitle(getString(R.string.readd_passphrase_title));
							check2.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
							{
								@Override public void onDialogConfirmed(DialogInterface dialog, String input)
								{
									if (input.equals(pin.toString()))
									{
										PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
											.putString("failsafe_check_key", Base64.encodeToString(EncryptionHelper.encrypt(pin.toString(), pin.toString()), Base64.NO_WRAP))
											.apply();
									}
									else
									{
										((SwitchPreferenceCompat)preference).setChecked(false);

										SnackBar.show(getActivity(), getString(R.string.passphrase_error), Snackbar.LENGTH_LONG, null);
									}
								}
							});

							check1.show(((FragmentActivity)getActivity()).getSupportFragmentManager(), null);
						}
					})
					.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							((SwitchPreferenceCompat)preference).setChecked(false);
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
			Intent backupIntent = new Intent(getActivity(), BackupService.class);
			AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(PendingIntent.getBroadcast(getActivity(), 0, backupIntent, (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0)));

			if ((Boolean)newValue)
			{
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("auto_backup", true).apply();
				((MainApplication)getActivity().getApplication()).registerBackupService();
				SnackBar.show(getActivity(), getString(R.string.backup_enable_toast, BackupHelper.FILES_PATH), Snackbar.LENGTH_LONG, null);
			}

			return true;
		}

		return false;
	}

	@Override public boolean onPreferenceClick(Preference preference)
	{
		Intent refresh = new Intent();
		refresh.putExtra("refresh", true);
		getActivity().setResult(RESULT_OK, refresh);

		if ("delivery_unit".equals(preference.getKey()))
		{
			final String[] options = new String[Unit.values().length];
			int index = 0, selectedIndex = Unit.getSelectedDeliveryUnit(getActivity()).ordinal();
			for (Unit unit : Unit.values())
			{
				options[index++] = unit.getLabel();
			}

			new AlertDialog.Builder(getActivity())
				.setTitle(R.string.select_measurement_title)
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("delivery_unit", index)
							.apply();

						findPreference("delivery_unit").setSummary(Html.fromHtml(getString(R.string.settings_delivery, Unit.getSelectedDeliveryUnit(getActivity()).getLabel())));
					}
				})
				.show();

			return true;
		}
		else if ("tds_unit".equals(preference.getKey()))
		{
			final String[] options = new String[TdsUnit.values().length];
			int index = 0, selectedIndex = TdsUnit.getSelectedTdsUnit(getActivity()).ordinal();
			for (TdsUnit unit : TdsUnit.values())
			{
				options[index++] = getString(unit.getStrRes()) + " (" + unit.getLabel() + ")";
			}

			new AlertDialog.Builder(getActivity())
				.setTitle(R.string.settings_tds_title)
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("tds_unit", index)
							.apply();

						findPreference("tds_unit").setSummary(Html.fromHtml(getString(R.string.settings_tds_summary, getString(TdsUnit.getSelectedTdsUnit(getActivity()).getStrRes()))));
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
				.setTitle(R.string.select_measurement_title)
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("measurement_unit", index)
							.apply();

					findPreference("measurement_unit").setSummary(Html.fromHtml(getString(R.string.settings_measurement, Unit.getSelectedMeasurementUnit(getActivity()).getLabel())));
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
				.setTitle(R.string.select_temperature_title)
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("temperature_unit", index)
							.apply();

						findPreference("temperature_unit").setSummary(Html.fromHtml(getString(R.string.settings_measurement, TempUnit.getSelectedTemperatureUnit(getActivity()).getLabel())));
					}
				})
				.show();

			return true;
		}
		else if ("default_garden".equals(preference.getKey()))
		{
			final String[] options = new String[GardenManager.getInstance().getGardens().size() + 1];
			options[0] = getString(R.string.all);
			int index = 0, selectedIndex = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("default_garden", -1) + 1;
			for (Garden garden : GardenManager.getInstance().getGardens())
			{
				options[++index] = garden.getName();
			}

			new AlertDialog.Builder(getActivity())
				.setTitle(R.string.select_garden_title)
				.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int index)
					{
						dialogInterface.dismiss();

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putInt("default_garden", index - 1)
							.apply();

						String defaultGarden = index - 1 > -1 ? GardenManager.getInstance().getGardens().get(index - 1).getName() : "All";
						findPreference("default_garden").setSummary(Html.fromHtml(getString(R.string.settings_default_garden, defaultGarden)));
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

			AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
				.setMessage(Html.fromHtml(readme))
				.create();
			alertDialog.show();

			if (alertDialog.findViewById(android.R.id.message) != null)
			{
//				Linkify.addLinks((TextView)alertDialog.findViewById(android.R.id.message), Linkify.WEB_URLS);
				((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
				((TextView)alertDialog.findViewById(android.R.id.message)).setLinksClickable(true);
			}

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
			String currentBackup = findPreference("backup_size").getSharedPreferences().getString("backup_size", "20");
			SnackBar.show(getActivity(), getString(R.string.backed_up_to) + BackupHelper.backupJson().getPath(), Snackbar.LENGTH_LONG, null);
			findPreference("backup_size").setSummary(Html.fromHtml(getString(R.string.settings_backup_size, currentBackup, lengthToString(BackupHelper.backupSize()))));
			findPreference("backup_now").setSummary(Html.fromHtml(getString(R.string.settings_lastbackup_summary, BackupHelper.getLastBackup())));
		}
		else if ("restore".equals(preference.getKey()))
		{
			// get list of backups
			File backupPath = new File(BackupHelper.FILES_PATH);
			String[] backupFiles = backupPath.list();

			if (backupFiles == null || backupFiles.length == 0)
			{
				SnackBar.show(getActivity(), getString(R.string.no_backups), Snackbar.LENGTH_LONG, null);
				return false;
			}

			Arrays.sort(backupFiles);
			final ArrayList<BackupData> backups = new ArrayList();

			BackupData current = new BackupData();
			Date lastDate = null;
			for (String backup : backupFiles)
			{
				File backupFile = new File(backup);
				String[] parts = backupFile.getName().split("\\.");
				Date date = new Date();
				if (parts.length > 1)
				{
					try
					{
						date = new Date(Long.parseLong(parts[0]));
					}
					catch (NumberFormatException e)
					{
						try
						{
							date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse(parts[0]);
						}
						catch (Exception e2)
						{
							date = new Date(backupFile.lastModified());
						}
					}

					if (parts.length == 2)
					{
						BackupData backupData = new BackupData();
						backupData.plantsPath = backupPath.getPath() + "/" + backup;
						backupData.date = date;
						backupData.size = backupPath.length();
						backups.add(backupData);
						continue;
					}

					if (lastDate == null || !date.equals(lastDate))
					{
						lastDate = date;
						current = new BackupData();
						current.date = date;
						backups.add(current);
					}
				}
				else
				{
					continue;
				}

				File file = new File(backupPath.getPath() + "/" + backup);
				if (backup.contains("plants"))
				{
					current.requireMigration = !backup.endsWith(".bak");
					current.plantsPath = backupPath.getPath() + "/" + backup;
					current.size += file.length();
				}

				if (backup.contains("gardens"))
				{
					current.requireMigration = !backup.endsWith(".bak");
					current.gardenPath = backupPath.getPath() + "/" + backup;
					current.size += file.length();
				}

				if (backup.contains("schedules"))
				{
					current.requireMigration = !backup.endsWith(".bak");
					current.schedulePath = backupPath.getPath() + "/" + backup;
					current.size += file.length();
				}
			}

			Collections.sort(backups, new Comparator<BackupData>()
			{
				@Override public int compare(BackupData o1, BackupData o2)
				{
					if (o1.date.before(o2.date)) return 1;
					if (o1.date.after(o2.date)) return -1;
					else return 0;
				}
			});
			CharSequence[] items = new CharSequence[backups.size()];
			for (int index = 0, count = backups.size(); index < count; index++)
			{
				items[index] = backups.get(index).toString();
			}

			new AlertDialog.Builder(getActivity())
				.setTitle(R.string.select_backup_title)
				.setItems(items, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						final BackupData selectedBackup = backups.get(which);

						if ((MainApplication.isFailsafe()))
						{
							MainApplication.setFailsafe(false);
						}

						if (selectedBackup.plantsPath == null) return;

						if (selectedBackup.plantsPath.endsWith(".dat.bak"))
						{
							final PinDialogFragment check = new PinDialogFragment();
							check.setTitle(getString(R.string.passphrase_title));
							check.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
							{
								@Override public void onDialogConfirmed(DialogInterface dialog, String input)
								{
									//decrypt file
									if (selectedBackup.plantsPath != null)
									{
										FileManager.getInstance().decryptTo(selectedBackup.plantsPath, selectedBackup.plantsPath + ".temp", input);

										if (MainApplication.isEncrypted())
										{
											FileManager.getInstance().encryptTo(selectedBackup.plantsPath + ".temp", selectedBackup.plantsPath + ".temp2", MainApplication.getKey());
											new File(selectedBackup.plantsPath + ".temp2").renameTo(new File(selectedBackup.plantsPath + ".temp"));
										}

										selectedBackup.plantsPath = selectedBackup.plantsPath + ".temp";
									}

									if (selectedBackup.gardenPath != null)
									{
										FileManager.getInstance().decryptTo(selectedBackup.gardenPath, selectedBackup.gardenPath + ".temp", input);

										if (MainApplication.isEncrypted())
										{
											FileManager.getInstance().encryptTo(selectedBackup.gardenPath + ".temp", selectedBackup.gardenPath + ".temp2", MainApplication.getKey());
											new File(selectedBackup.gardenPath + ".temp2").renameTo(new File(selectedBackup.gardenPath + ".temp"));
										}

										selectedBackup.gardenPath = selectedBackup.gardenPath + ".temp";
									}

									if (selectedBackup.schedulePath != null)
									{
										FileManager.getInstance().decryptTo(selectedBackup.schedulePath, selectedBackup.schedulePath + ".temp", input);

										if (MainApplication.isEncrypted())
										{
											FileManager.getInstance().encryptTo(selectedBackup.schedulePath + ".temp", selectedBackup.schedulePath + ".temp2", MainApplication.getKey());
											new File(selectedBackup.schedulePath + ".temp2").renameTo(new File(selectedBackup.schedulePath + ".temp"));
										}

										selectedBackup.schedulePath = selectedBackup.schedulePath + ".temp";
									}

									if (new File(selectedBackup.plantsPath).exists())
									{
										completeRestore(selectedBackup);

										if (selectedBackup.plantsPath != null && selectedBackup.plantsPath.endsWith(".temp"))
										{
											FileManager.getInstance().removeFile(selectedBackup.plantsPath);
										}
										if (selectedBackup.gardenPath != null && selectedBackup.gardenPath.endsWith(".temp"))
										{
											FileManager.getInstance().removeFile(selectedBackup.gardenPath);
										}
										if (selectedBackup.schedulePath != null && selectedBackup.schedulePath.endsWith(".temp"))
										{
											FileManager.getInstance().removeFile(selectedBackup.schedulePath);
										}

										check.dismiss();
									}
									else
									{
										check.getInput().setError(getString(R.string.encrypt_passphrase_error));
									}
								}
							});
							check.show(getChildFragmentManager(), null);
							return;
						}
						else if (MainApplication.isEncrypted())
						{
							//encrypt an unencrypted file
							if (selectedBackup.plantsPath != null)
							{
								FileManager.getInstance().encryptTo(selectedBackup.plantsPath, selectedBackup.plantsPath + ".temp", MainApplication.getKey());
								selectedBackup.plantsPath = selectedBackup.plantsPath + ".temp";
							}

							if (selectedBackup.gardenPath != null)
							{
								FileManager.getInstance().encryptTo(selectedBackup.gardenPath, selectedBackup.gardenPath + ".temp", MainApplication.getKey());
								selectedBackup.gardenPath = selectedBackup.gardenPath + ".temp";
							}

							if (selectedBackup.schedulePath != null)
							{
								FileManager.getInstance().encryptTo(selectedBackup.schedulePath, selectedBackup.schedulePath + ".temp", MainApplication.getKey());
								selectedBackup.schedulePath = selectedBackup.schedulePath + ".temp";
							}

							completeRestore(selectedBackup);

							if (selectedBackup.plantsPath != null && selectedBackup.plantsPath.endsWith(".temp"))
							{
								FileManager.getInstance().removeFile(selectedBackup.plantsPath);
							}
							if (selectedBackup.gardenPath != null && selectedBackup.gardenPath.endsWith(".temp"))
							{
								FileManager.getInstance().removeFile(selectedBackup.gardenPath);
							}
							if (selectedBackup.schedulePath != null && selectedBackup.schedulePath.endsWith(".temp"))
							{
								FileManager.getInstance().removeFile(selectedBackup.schedulePath);
							}
						}
						else
						{
							completeRestore(selectedBackup);
						}
					}
				})
				.show();
		}
		else if ("image_location".equals(preference.getKey()))
		{
			if (Build.VERSION.SDK_INT >= 21)
			{
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				startActivityForResult(intent, REQUEST_PICK_DOCUMENT);
			}
		}
		else if ("backup_location".equals(preference.getKey()))
		{
			if (Build.VERSION.SDK_INT >= 21)
			{
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				startActivityForResult(intent, REQUEST_PICK_BACKUP_DOCUMENT);
			}
		}
		else if ("clear_image_cache".equals(preference.getKey()))
		{
			ImageLoader.getInstance().clearDiskCache();
			ImageLoader.getInstance().clearMemoryCache();
			SnackBar.show(getActivity(), getString(R.string.cache_cleared), Snackbar.LENGTH_SHORT, null);
		}
		else if ("import".equals(preference.getKey()))
		{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");

			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

			startActivityForResult(intent, REQUEST_PICK_IMPORT_DOCUMENT);
		}

		return false;
	}

	private void completeRestore(BackupData selectedBackup)
	{
		FileManager.getInstance().copyFile(PlantManager.FILES_DIR + "/plants." + PlantManager.getInstance().getFileExt(), PlantManager.FILES_DIR + "/plants.temp");
		FileManager.getInstance().copyFile(selectedBackup.plantsPath, PlantManager.FILES_DIR + "/plants." + PlantManager.getInstance().getFileExt());
		boolean loaded = PlantManager.getInstance().load(true);

		if (selectedBackup.gardenPath != null)
		{
			FileManager.getInstance().copyFile(GardenManager.FILES_DIR + "/gardens." + GardenManager.getInstance().getFileExt(), GardenManager.FILES_DIR + "/gardens.temp");
			FileManager.getInstance().copyFile(selectedBackup.gardenPath, GardenManager.FILES_DIR + "/gardens." + GardenManager.getInstance().getFileExt());
			GardenManager.getInstance().load();
		}

		if (selectedBackup.schedulePath != null)
		{
			FileManager.getInstance().copyFile(ScheduleManager.FILES_DIR + "/schedules." + ScheduleManager.instance.getFileExt(), ScheduleManager.FILES_DIR + "/schedules.temp");
			FileManager.getInstance().copyFile(selectedBackup.schedulePath, ScheduleManager.FILES_DIR + "/schedules." + ScheduleManager.instance.getFileExt());
			ScheduleManager.instance.load();
		}

		if (!loaded)
		{
			String errorEnd = MainApplication.isEncrypted() ? getString(R.string.unencrypted) : getString(R.string.encrypted);
			SnackBar.show(getActivity(), getString(R.string.restore_error, selectedBackup.toString(), errorEnd), Snackbar.LENGTH_INDEFINITE, null);
			FileManager.getInstance().copyFile(PlantManager.FILES_DIR + "/plants.temp", PlantManager.FILES_DIR + "/plants." + PlantManager.getInstance().getFileExt());
			FileManager.getInstance().copyFile(GardenManager.FILES_DIR + "/gardens.temp", GardenManager.FILES_DIR + "/gardens.json");
			FileManager.getInstance().copyFile(ScheduleManager.FILES_DIR + "/schedules.temp", ScheduleManager.FILES_DIR + "/schedules.json");
			PlantManager.getInstance().load();
			GardenManager.getInstance().load();
			ScheduleManager.instance.load();
		}
		else
		{
			SnackBar.show(getActivity(), getString(R.string.restore_complete, selectedBackup.toString()), Snackbar.LENGTH_LONG, null);
		}

		FileManager.getInstance().removeFile(PlantManager.FILES_DIR + "/plants.temp");
		FileManager.getInstance().removeFile(GardenManager.FILES_DIR + "/gardens.temp");
		FileManager.getInstance().removeFile(ScheduleManager.FILES_DIR + "/schedules.temp");

		if (selectedBackup.requireMigration)
		{
			MigrationHelper.migratePpm(getActivity());
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_UNINSTALL)
		{
			// refresh addons
			SnackBar.show(getActivity(), "Addon successfully uninstalled", Snackbar.LENGTH_SHORT, null);
			populateAddons();
		}
		else if (requestCode == REQUEST_PICK_DOCUMENT && Build.VERSION.SDK_INT >= 19)
		{
			if (resultCode == RESULT_OK)
			{
				Uri treeUri = data.getData();
				DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);

				Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));

				if (pickedDir != null)
				{
					String filePath = null;
					try
					{
						filePath = PathHelper.getPath(getActivity(), docUri);

						if (!TextUtils.isEmpty(filePath) && new File(filePath).exists())
						{
							if (!filePath.endsWith("/")) filePath = filePath + "/";

							if (new File(filePath).canWrite())
							{
								findPreference("image_location").getSharedPreferences().edit().putString("image_location", filePath).apply();
								findPreference("image_location").setSummary(Html.fromHtml(getString(R.string.settings_image_location_summary, filePath)));

								return;
							}
						}
					}
					catch (URISyntaxException e)
					{
					}
				}
			}

			SnackBar.show(getActivity(), getString(R.string.settings_image_location_error), Snackbar.LENGTH_LONG, null);
		}
		else if (requestCode == REQUEST_PICK_BACKUP_DOCUMENT && Build.VERSION.SDK_INT >= 19)
		{
			if (resultCode == RESULT_OK)
			{
				Uri treeUri = data.getData();
				DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);

				Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));

				if (pickedDir != null)
				{
					String filePath = null;
					try
					{
						filePath = PathHelper.getPath(getActivity(), docUri);

						if (!TextUtils.isEmpty(filePath) && new File(filePath).exists())
						{
							if (!filePath.endsWith("/")) filePath = filePath + "/";

							if (new File(filePath).canWrite())
							{
								BackupHelper.FILES_PATH = filePath;
								findPreference("backup_location").getSharedPreferences().edit().putString("backup_location", filePath).apply();
								findPreference("backup_location").setSummary(Html.fromHtml(getString(R.string.settings_backup_location_summary, filePath)));

								// refresh backup size
								String currentBackup = findPreference("backup_size").getSharedPreferences().getString("backup_size", "20");
								findPreference("backup_size").setSummary(Html.fromHtml(getString(R.string.settings_backup_size, currentBackup, lengthToString(BackupHelper.backupSize()))));

								// refresh last backup
								findPreference("backup_now").setSummary(Html.fromHtml(getString(R.string.settings_lastbackup_summary, BackupHelper.getLastBackup())));
								return;
							}
						}
					}
					catch (URISyntaxException e)
					{
					}
				}
			}

			SnackBar.show(getActivity(), getString(R.string.settings_backup_location_error), Snackbar.LENGTH_LONG, null);
		}
		else if (requestCode == REQUEST_PICK_IMPORT_DOCUMENT)
		{
			if (resultCode == RESULT_OK && data.getData() != null)
			{
				try
				{
					File temp = new File(getActivity().getCacheDir(), "importtemp.json");
					InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
					FileManager.getInstance().copyFile(inputStream, new FileOutputStream(temp));

					if (temp.exists())
					{
						// Try reading as plants
						ArrayList<Plant> plants = MoshiHelper.parse(temp, Types.newParameterizedType(ArrayList.class, Plant.class));
						if (plants != null)
						{
							// backup
							FileManager.getInstance().copyFile(PlantManager.FILES_DIR + "/plants." + PlantManager.getInstance().getFileExt(), PlantManager.FILES_DIR + "/plants." + PlantManager.getInstance().getFileExt() + ".bak");
							FileManager.getInstance().copyFile(temp.getPath(), PlantManager.FILES_DIR + "/plants." + PlantManager.getInstance().getFileExt());
							PlantManager.getInstance().load();
							SnackBar.show(getActivity(), getString(R.string.settings_import_success), Snackbar.LENGTH_LONG, null);
							return;
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			SnackBar.show(getActivity(), getString(R.string.settings_import_error), Snackbar.LENGTH_LONG, null);
		}
	}

	public String lengthToString(long bytes)
	{
		int unit = 1024;
		if (bytes < unit)
		{
			return bytes + " B";
		}

		int exp = (int)(Math.log(bytes) / Math.log(unit));
		String pre = "KMGTPE".charAt(exp - 1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public class BackupData
	{
		Date date;
		String plantsPath;
		String gardenPath;
		String schedulePath;
		long size = 0;
		boolean requireMigration = false;

		@Override public String toString()
		{
			boolean encrypted = plantsPath != null && plantsPath.endsWith(".dat.bak");
			String out = "(" + (encrypted ? "encrypted " : "") + lengthToString(size) + ")";
			if (getActivity() != null)
			{
				DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
				DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
				out = dateFormat.format(date) + " " + timeFormat.format(date) + " " + out;
			}
			else
			{
				out = date + " " + out;
			}

			return out;
		}
	}
}
