package me.anon.grow

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import me.anon.grow.fragment.WateringFragment

class EditWateringActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
		setTitle(R.string.edit_feeding)

		var plantIndex = -1
		var feedingIndex = -1

		if (intent.extras != null)
		{
			plantIndex = intent.extras!!.getInt("plant_index", -1)
			feedingIndex = intent.extras!!.getInt("action_index", -1)
		}

		if (plantIndex < 0)
		{
			finish()
			return
		}

		if (supportFragmentManager.findFragmentByTag("fragment") == null)
		{
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_holder, WateringFragment.newInstance(intArrayOf(plantIndex), feedingIndex), "fragment")
				.commit()
		}
	}
}
