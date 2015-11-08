package me.anon.grow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import me.anon.grow.fragment.PinDialogFragment;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.manager.PlantManager;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class BootActivity extends Activity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (MainApplication.isEncrypted())
		{
			final PinDialogFragment check = new PinDialogFragment();
			check.setTitle("Enter your passphrase");
			check.setOnDialogConfirmed(new PinDialogFragment.OnDialogConfirmed()
			{
				@Override public void onDialogConfirmed(String input)
				{
					String integrityCheck = PreferenceManager.getDefaultSharedPreferences(BootActivity.this).getString("encryption_check_key", "");
					String inputCheck = Base64.encodeToString(EncryptionHelper.encrypt(input, input), Base64.NO_WRAP);

					if (inputCheck.equals(integrityCheck))
					{
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

			check.show(getFragmentManager(), null);
		}
		else
		{
			start();
		}
	}

	private void start()
	{
		Intent main = new Intent(this, MainActivity.class);
		startActivity(main);
		finish();
	}
}
