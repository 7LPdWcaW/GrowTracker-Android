package me.anon.grow;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import me.anon.grow.fragment.WateringFragment;
import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class AddWateringActivity extends BaseActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_holder);
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
		Views.inject(this);

		int[] plantIndex = null;
		if (getIntent().getExtras() != null)
		{
			plantIndex = getIntent().getExtras().getIntArray("plant_index");

			if (plantIndex == null)
			{
				plantIndex = new int[]{-1};
			}
		}

		if (plantIndex == null || plantIndex.length == 0)
		{
			finish();
			return;
		}

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, WateringFragment.newInstance(plantIndex, -1), TAG_FRAGMENT).commit();
		}
	}
}
