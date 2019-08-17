package me.anon.grow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

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

	@Override protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		boolean forceDark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("force_dark", false);
		AppCompatDelegate.setDefaultNightMode(forceDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
	}

	@Override protected void onResume()
	{
		super.onResume();

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		sendBroadcast(new Intent("me.anon.grow.ACTION_UPDATER"));
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			// ew
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
