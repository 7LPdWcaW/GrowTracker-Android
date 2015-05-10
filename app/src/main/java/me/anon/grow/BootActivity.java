package me.anon.grow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class BootActivity extends Activity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent main = new Intent(this, MainActivity.class);
		startActivity(main);
		finish();
	}
}
