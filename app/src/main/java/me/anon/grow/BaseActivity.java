package me.anon.grow;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;

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

		Intent otherIntents = new Intent("me.anon.grow.ACTION_UPDATER");
		List<ResolveInfo> resolveInfos = getPackageManager().queryBroadcastReceivers(otherIntents, PackageManager.GET_META_DATA);

		if (resolveInfos.size() > 0)
		{
			for (final ResolveInfo resolveInfo : resolveInfos)
			{
				try
				{
					String appName = (String)resolveInfo.loadLabel(getPackageManager());
					appName = TextUtils.isEmpty(appName) ? resolveInfo.activityInfo.packageName : appName;

					Intent broadcast = new Intent();
					broadcast.setAction("me.anon.grow.ACTION_UPDATER");
					broadcast.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
					broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					sendBroadcast(broadcast);
				}
				catch (Exception e)
				{

				}
			}
		}
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
