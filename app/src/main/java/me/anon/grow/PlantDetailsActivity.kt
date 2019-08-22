package me.anon.grow

import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.fragment.PlantDetailsFragment

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
class PlantDetailsActivity : BaseActivity()
{
	public val toolbarLayout: AppBarLayout by lazy { toolbar_layout }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		if (!checkEncryptState())
		{
			super.onCreate(savedInstanceState)

			setContentView(R.layout.fragment_holder)
			setSupportActionBar(toolbar)
			supportActionBar?.setDisplayHomeAsUpEnabled(true)
			supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp)

			supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) ?: let {
				supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, PlantDetailsFragment.newInstance(intent.extras), TAG_FRAGMENT).commit()
			}
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		if (item.itemId == android.R.id.home)
		{
			val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder)

			if (fragment is PlantDetailsFragment)
			{
				fragment.save()
			}

			return true
		}

		return super.onOptionsItemSelected(item)
	}

	companion object
	{
		private val TAG_FRAGMENT = "current_fragment"
	}
}
