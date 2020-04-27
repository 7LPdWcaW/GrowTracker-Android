package me.anon.grow

import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.fragment.StatisticsFragment
import me.anon.grow.fragment.StatisticsFragment2
import me.anon.lib.manager.PlantManager

class StatisticsActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		setSupportActionBar(toolbar)

//		if (intent.extras == null || !intent.hasExtra("plant"))
//		{
//			finish()
//			return
//		}

		intent.putExtra("plant", PlantManager.instance.plants[0])

		if (supportFragmentManager.findFragmentByTag("fragment") == null)
		{
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_holder, StatisticsFragment2.newInstance(intent.extras!!), "fragment")
				.commit()
		}
	}
}
