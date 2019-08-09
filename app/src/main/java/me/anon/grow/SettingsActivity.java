package me.anon.grow;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import me.anon.grow.fragment.SettingsFragment;
import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class SettingsActivity extends BaseActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_holder);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
		Views.inject(this);

		setTitle(R.string.nav_settings);

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, new SettingsFragment(), TAG_FRAGMENT).commit();
		}
	}
}
