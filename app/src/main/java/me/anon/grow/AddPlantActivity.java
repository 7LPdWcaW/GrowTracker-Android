package me.anon.grow;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import me.anon.grow.fragment.PlantDetailsFragment;
import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class AddPlantActivity extends BaseActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_holder);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
		Views.inject(this);

		int gardenIndex = -1;

		if (getIntent().getExtras() != null)
		{
			gardenIndex = getIntent().getExtras().getInt("garden_index", -1);
		}

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, PlantDetailsFragment.newInstance(-1, gardenIndex), TAG_FRAGMENT).commit();
		}
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_holder);

			if (fragment instanceof PlantDetailsFragment)
			{
				((PlantDetailsFragment)fragment).save();
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
