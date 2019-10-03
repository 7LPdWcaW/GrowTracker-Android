package me.anon.grow

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_holder.*

import me.anon.grow.fragment.WateringFragment
import me.anon.lib.Views
import me.anon.lib.manager.PlantManager

class AddWateringActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		setSupportActionBar(toolbar)

		var plantIndex: IntArray? = intent.extras?.getIntArray("plant_index") ?: intArrayOf(-1)

		if (plantIndex == null || plantIndex.size == 0)
		{
			finish()
			return
		}

		if (supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) == null)
		{
			supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, WateringFragment.newInstance(plantIndex, -1), TAG_FRAGMENT).commit()
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
