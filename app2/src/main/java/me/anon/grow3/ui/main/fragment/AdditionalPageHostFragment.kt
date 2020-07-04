package me.anon.grow3.ui.main.fragment

import android.os.Bundle
import androidx.fragment.app.commitNow
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentHostBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.main.activity.MainActivity

class AdditionalPageHostFragment : BaseHostFragment(FragmentHostBinding::class)
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		if (savedInstanceState == null && arguments != null)
		{
			try
			{
				val page = Class.forName(requireArguments().getString(MainActivity.EXTRA_NAVIGATE)!!).newInstance() as BaseFragment
				page.arguments = arguments

				childFragmentManager.commitNow {
					setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
					replace(R.id.main_content, page, "fragment")
				}
			}
			catch (e: Exception)
			{
				e.printStackTrace()
			}
		}
	}
}
