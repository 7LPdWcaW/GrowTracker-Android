package me.anon.grow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.fragment.FeedingScheduleDetailsFragment

/**
 * Activity holder for feeding schedule list
 */
class FeedingScheduleDetailsActivity : AppCompatActivity()
{
	companion object
	{
		public const val TAG_FRAGMENT = "fragment"
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		setSupportActionBar(toolbar)
		setTitle(R.string.schedule_details_title)

		if (fragmentManager.findFragmentByTag(TAG_FRAGMENT) == null)
		{
			fragmentManager.beginTransaction().replace(R.id.fragment_holder, FeedingScheduleDetailsFragment.newInstance(intent.extras?.getInt("schedule_index", -1) ?: -1), TAG_FRAGMENT).commit()
		}
	}

	override fun onBackPressed()
	{
		if ((fragmentManager.findFragmentByTag(TAG_FRAGMENT) as FeedingScheduleDetailsFragment).onBackPressed())
		{
			super.onBackPressed()
		}
	}
}
