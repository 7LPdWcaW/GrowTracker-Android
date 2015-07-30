package me.anon.grow;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import lombok.experimental.Accessors;
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
@Accessors(prefix = {"m", ""}, chain = true)
public class PlantDetailsActivity extends AppCompatActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);

		setContentView(R.layout.fragment_holder);
		Views.inject(this);

		int plantIndex = -1;
		if (getIntent().getExtras() != null)
		{
			plantIndex = getIntent().getExtras().getInt("plant_index", -1);
		}

		if (plantIndex < 0)
		{
			finish();
			return;
		}

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, PlantDetailsFragment.newInstance(plantIndex), TAG_FRAGMENT).commit();
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
