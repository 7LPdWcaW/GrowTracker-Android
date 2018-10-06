package me.anon.grow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.fragment.FeedingScheduleListFragment

/**
 * Activity holder for feeding schedule list
 */
class FeedingScheduleActivity : AppCompatActivity()
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
		setTitle(R.string.feeding_schedules_title)

		if (fragmentManager.findFragmentByTag(TAG_FRAGMENT) == null)
		{
			fragmentManager.beginTransaction().replace(R.id.fragment_holder, FeedingScheduleListFragment(), TAG_FRAGMENT).commit()
		}
	}
}
