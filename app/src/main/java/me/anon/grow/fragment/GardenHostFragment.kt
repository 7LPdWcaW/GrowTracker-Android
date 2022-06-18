package me.anon.grow.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.anon.grow.R
import me.anon.grow.databinding.GardenFragmentHolderBinding
import me.anon.model.Garden

class GardenHostFragment : Fragment()
{
	public lateinit var garden: Garden

	private lateinit var binding: GardenFragmentHolderBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= GardenFragmentHolderBinding.inflate(inflater).let {
		binding = it
		it.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		savedInstanceState?.let {
			garden = it.getParcelable("garden")!!
		}

		childFragmentManager.findFragmentByTag("child_fragment") ?: let {
			childFragmentManager.beginTransaction().replace(R.id.fragment_holder, GardenFragment.newInstance(garden), "child_fragment").commit()
		}

		binding.tabs.setOnNavigationItemSelectedListener {
			childFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
				.replace(R.id.fragment_holder, when (it.itemId)
				{
					R.id.view_plants -> GardenFragment.newInstance(garden)
					R.id.view_history -> GardenTrackerFragment.newInstance(garden)
					else -> Fragment()
				}, "child_fragment")
				.commit()

			return@setOnNavigationItemSelectedListener true
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		super.onActivityResult(requestCode, resultCode, data)
		childFragmentManager.findFragmentById(R.id.fragment_holder)?.onActivityResult(requestCode, resultCode, data)
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		outState.putParcelable("garden", garden)
		super.onSaveInstanceState(outState)
	}

	companion object
	{
		@JvmStatic
		fun newInstance(garden: Garden): GardenHostFragment
		{
			val fragment = GardenHostFragment()
			fragment.garden = garden

			return fragment
		}
	}
}
