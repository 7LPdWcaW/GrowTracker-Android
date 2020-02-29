package me.anon.grow

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.fragment.WateringFragment

class AddWateringActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		setSupportActionBar(toolbar)

		var plantIndex: IntArray? = intent.extras?.getIntArray("plant_index") ?: intArrayOf(-1)
		var gardenIndex: Int = intent.extras?.getInt("garden_index") ?: -1

		if (plantIndex == null || plantIndex.size == 0)
		{
			finish()
			return
		}

		if (supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) == null)
		{
			supportFragmentManager.beginTransaction().replace(R.id.coordinator, WateringFragment.newInstance(plantIndex, -1, gardenIndex), TAG_FRAGMENT).commit()
		}
	}

	override fun onBackPressed()
	{
		AlertDialog.Builder(this)
			.setTitle(R.string.confirm_title)
			.setMessage(R.string.confirm_quit)
			.setPositiveButton(R.string.accept_quit, { index, dialog ->
				super.onBackPressed()
			})
			.setNegativeButton(R.string.cancel, null)
			.show()
	}

	companion object
	{
		private val TAG_FRAGMENT = "current_fragment"
	}
}
