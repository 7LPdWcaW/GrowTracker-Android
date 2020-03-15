package me.anon.grow;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import me.anon.grow.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_holder);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

		setTitle(R.string.nav_settings);

		if (getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_holder, new SettingsFragment(), TAG_FRAGMENT)
				.commit();
		}
	}
}
