package me.anon.grow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Base64;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import me.anon.grow.fragment.PinDialogFragment;
import me.anon.lib.handler.ExceptionHandler;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.MigrationHelper;
import me.anon.lib.manager.PlantManager;

public class BootActivity extends Activity
{
	private boolean sentIntent = false;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final String[] exceptions = ExceptionHandler.getInstance().searchForStackTraces();
		if (exceptions != null && exceptions.length > 0)
		{
			new AlertDialog.Builder(this)
				.setTitle("Uh-oh")
				.setMessage(Html.fromHtml("Looks like there was a crash the last time you used the app. Would you like to send these anonymous reports? " +
					"These reports will be sent to <a href=\"https://github.com/7LPdWcaW/GrowTracker-Android/issues\">github.com/7LPdWcaW/GrowTracker-Android/issues</a>, no personal information will be included. You can optionally " +
					"post these reports to <a href=\"https://reddit.com/r/growutils\">reddit.com/r/growutils</a> manually if you wish. Reports are stored in <i>" + ExceptionHandler.getInstance().getFilesPath() + "</i>"))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
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
							Uri u = Uri.fromFile(fileIn);
							uris.add(u);
						}

						share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
						startActivity(Intent.createChooser(share, "Send mail..."));
						sentIntent = true;
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener()
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
			check.setTitle("Enter your passphrase");
			check.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
			{
				@Override public void onDialogConfirmed(String input)
				{
					String integrityCheck = PreferenceManager.getDefaultSharedPreferences(BootActivity.this).getString("encryption_check_key", "");
					String failsafeCheck = PreferenceManager.getDefaultSharedPreferences(BootActivity.this).getString("failsafe_check_key", "");
					String inputCheck = Base64.encodeToString(EncryptionHelper.encrypt(input, input), Base64.NO_WRAP);

					if (inputCheck.equalsIgnoreCase(failsafeCheck))
					{
						MainApplication.setFailsafe(true);
						start();
					}
					else if (inputCheck.equals(integrityCheck))
					{
						MainApplication.setFailsafe(false);
						MainApplication.setKey(input);
						PlantManager.getInstance().load();

						start();
					}
					else
					{
						Toast.makeText(BootActivity.this, "Error - incorrect passphrase", Toast.LENGTH_SHORT).show();

						check.dismiss();
						check.show(getFragmentManager(), null);
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

			check.show(getFragmentManager(), null);
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
			MigrationHelper.performMigration(this);
			start();
		}
		else
		{
			Intent main = new Intent(this, MainActivity.class);
			startActivity(main);
			finish();
		}
	}
}
