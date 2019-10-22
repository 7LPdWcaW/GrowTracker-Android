package me.anon.grow

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import me.anon.grow.fragment.WateringFragment
import me.anon.lib.manager.PlantManager

class EditWateringActivity : BaseActivity()
{
	private var plantIndex: Int = -1
	private var feedingIndex: Int = -1

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
		setTitle(R.string.edit_feeding)

		plantIndex = -1
		feedingIndex = -1

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
				.replace(R.id.coordinator, WateringFragment.newInstance(intArrayOf(plantIndex), feedingIndex), "fragment")
				.commit()
		}
	}

	override fun onBackPressed()
	{
		AlertDialog.Builder(this)
			.setTitle(R.string.confirm_title)
			.setMessage(R.string.confirm_quit)
			.setPositiveButton(R.string.accept_quit, { index, dialog ->
				if (intent.extras?.getBoolean("new_water") == true)
				{
					PlantManager.instance.plants[plantIndex].actions?.removeAt(feedingIndex)
				}

				super.onBackPressed()
			})
			.setNegativeButton(R.string.cancel, null)
			.show()
	}
}
