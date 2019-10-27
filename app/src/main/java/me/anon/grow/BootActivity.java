package me.anon.grow;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Base64;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import me.anon.grow.fragment.PinDialogFragment;
import me.anon.lib.handler.ExceptionHandler;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.MigrationHelper;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.AsyncCallback;

public class BootActivity extends AppCompatActivity
{
	private boolean sentIntent = false;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		boolean forceDark = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).getBoolean("force_dark", false);
		AppCompatDelegate.setDefaultNightMode(forceDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

		super.onCreate(savedInstanceState);

		final String[] exceptions = ExceptionHandler.getInstance().searchForStackTraces();
		if (exceptions != null && exceptions.length > 0)
		{
			new AlertDialog.Builder(this)
				.setTitle(R.string.crash_title)
				.setMessage(Html.fromHtml(getString(R.string.crash_message, ExceptionHandler.getInstance().getFilesPath())))
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int i)
					{
						Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
						share.setType("*/*");
						share.putExtra(Intent.EXTRA_SUBJECT, "[Crash] crash report (do not change)");
						share.putExtra(Intent.EXTRA_EMAIL, new String[]{"7lpdwcaw@gmail.com"});

						ArrayList<Uri> uris = new ArrayList<Uri>();
						for (String file : exceptions)
						{
							File fileIn = new File(ExceptionHandler.getInstance().getFilesPath() + "/" + file);
							Uri u = FileProvider.getUriForFile(BootActivity.this, getPackageName() + ".provider", fileIn);
							uris.add(u);
						}

						String deviceInfo = "";
						deviceInfo += Build.BRAND + " ";
						deviceInfo += Build.MODEL + " ";
						deviceInfo += Build.DEVICE + ", ";
						deviceInfo += Build.MANUFACTURER + ", ";
						deviceInfo += Build.VERSION.SDK_INT + ", ";
						deviceInfo += BuildConfig.VERSION_NAME;

						share.putExtra(Intent.EXTRA_TEXT, deviceInfo);
						share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
						startActivity(Intent.createChooser(share, "Send mail..."));
						sentIntent = true;
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int i)
					{
						renameExceptions();
						startup();
					}
				})
				.setCancelable(false)
				.show();
		}
		else
		{
			startup();
		}
	}

	@Override protected void onResume()
	{
		super.onResume();

		if (sentIntent)
		{
			renameExceptions();
			startup();
		}
	}

	private void renameExceptions()
	{
		String[] exceptions = ExceptionHandler.getInstance().searchForStackTraces();

		if (exceptions != null && exceptions.length > 0)
		{
			for (String file : exceptions)
			{
				new File(ExceptionHandler.getInstance().getFilesPath() + file).renameTo(new File(ExceptionHandler.getInstance().getFilesPath() + file + ".sent"));
			}
		}
	}

	private void startup()
	{
		if (MainApplication.isEncrypted())
		{
			final PinDialogFragment check = new PinDialogFragment();
			check.setTitle(getString(R.string.passphrase_title));
			check.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
			{
				@Override public void onDialogConfirmed(DialogInterface dialog, String input)
				{
					String integrityCheck = PreferenceManager.getDefaultSharedPreferences(BootActivity.this).getString("encryption_check_key", Base64.encodeToString(EncryptionHelper.encrypt(input, input), Base64.NO_WRAP));
					String failsafeCheck = PreferenceManager.getDefaultSharedPreferences(BootActivity.this).getString("failsafe_check_key", "");
					String inputCheck = Base64.encodeToString(EncryptionHelper.encrypt(input, input), Base64.NO_WRAP);

					if (inputCheck.equalsIgnoreCase(failsafeCheck))
					{
						MainApplication.setFailsafe(true);
						start();
						dialog.dismiss();
					}
					else if (inputCheck.equals(integrityCheck))
					{
						MainApplication.setFailsafe(false);
						MainApplication.setKey(input);
						PlantManager.getInstance().load();

						start();
						dialog.dismiss();
					}
					else
					{
						check.getInput().setError(getString(R.string.encrypt_passphrase_error));
					}
				}
			});
			check.setOnDialogCancelled(new PinDialogFragment.OnDialogCancelled()
			{
				@Override public void onDialogCancelled()
				{
					finish();
				}
			});

			check.show(getSupportFragmentManager(), null);
		}
		else
		{
			start();
		}
	}

	private void start()
	{
		if (MigrationHelper.needsMigration(this))
		{
			MigrationHelper.performMigration(this, new AsyncCallback()
			{
				@Override public void callback()
				{
					start();
				}
			});
		}
		else
		{
			Intent main = new Intent(this, MainActivity.class);
			startActivity(main);
			finish();
		}
	}
}
