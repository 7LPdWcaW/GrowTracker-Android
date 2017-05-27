package me.anon.grow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

/**
 * Base activity that sends broadcast events on resume
 */
public class BaseActivity extends AppCompatActivity
{
	/**
	 * Checks if we need to reauth for decrypting or not,
	 * @return
	 */
	public boolean checkEncryptState()
	{
		// if we've resumed from death and we're in encrypted mode, we need to force re-auth
		if (MainApplication.isEncrypted() && TextUtils.isEmpty(MainApplication.getKey()))
		{
			Intent intent = new Intent(this, BootActivity.class);
			startActivity(intent);
			finishAffinity();
			System.exit(0);

			return true;
		}

		return false;
	}

	@Override protected void onResume()
	{
		super.onResume();

		sendBroadcast(new Intent("me.anon.grow.ACTION_UPDATER"));
	}
}
