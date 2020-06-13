package me.anon.grow3.ui.main.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentHostBinding
import me.anon.grow3.ui.base.BaseHostFragment

class AdditionalPageHostFragment : BaseHostFragment(FragmentHostBinding::class.java)
{
	public fun addPage(fragment: Fragment)
	{
		lifecycleScope.launchWhenResumed {
			childFragmentManager.commit {
				setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				add(R.id.fragment_container, fragment)
			}
		}
	}
}
