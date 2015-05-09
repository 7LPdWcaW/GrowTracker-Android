package me.anon.grow;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import lombok.experimental.Accessors;
import me.anon.grow.fragment.AddFeedingFragment;
import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
@Accessors(prefix = {"m", ""}, chain = true)
public class AddFeedingActivity extends AppCompatActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

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
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, AddFeedingFragment.newInstance(plantIndex), TAG_FRAGMENT).commit();
		}
	}
}
