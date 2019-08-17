package me.anon.grow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.fragment.ScheduleDateDetailsFragment

/**
 * Activity holder for feeding schedule list
 */
class ScheduleDateDetailsActivity : BaseActivity()
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
		setTitle(R.string.schedule_date_title)

		if (supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) == null)
		{
			supportFragmentManager.beginTransaction().replace(
				R.id.fragment_holder,
				ScheduleDateDetailsFragment.newInstance(
					intent.extras?.getInt("schedule_index", -1) ?: -1,
					intent.extras?.getInt("date_index", -1) ?: -1),
				TAG_FRAGMENT
			).commit()
		}
	}
}
