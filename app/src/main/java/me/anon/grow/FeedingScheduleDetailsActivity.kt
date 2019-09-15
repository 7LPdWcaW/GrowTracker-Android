package me.anon.grow

import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.fragment.FeedingScheduleDetailsFragment

/**
 * Activity holder for feeding schedule list
 */
class FeedingScheduleDetailsActivity : BaseActivity()
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

		if (supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) == null)
		{
			supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, FeedingScheduleDetailsFragment.newInstance(intent.extras), TAG_FRAGMENT).commit()
		}
	}

	override fun onBackPressed()
	{
		if ((supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as FeedingScheduleDetailsFragment).onBackPressed())
		{
			super.onBackPressed()
		}
	}
}
