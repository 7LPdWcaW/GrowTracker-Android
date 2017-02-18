package me.anon.grow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Base activity that sends broadcast events on resume
 */
public class BaseActivity extends AppCompatActivity
{
	@Override protected void onResume()
	{
		super.onResume();

		sendBroadcast(new Intent("me.anon.grow.ACTION_UPDATER"));
	}
}
