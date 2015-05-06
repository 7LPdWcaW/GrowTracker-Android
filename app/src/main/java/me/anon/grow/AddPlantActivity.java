package me.anon.grow;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import lombok.experimental.Accessors;
import me.anon.grow.fragment.PlantDetailsFragment;
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
public class AddPlantActivity extends AppCompatActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_holder);
		Views.inject(this);

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, PlantDetailsFragment.newInstance(null), TAG_FRAGMENT).commit();
		}
	}
}
