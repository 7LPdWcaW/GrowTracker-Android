package me.anon.grow3.ui.main.fragment

import androidx.core.view.updatePadding
import me.anon.grow3.databinding.FragmentNavigationBinding
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.settings.activity.SettingsActivity
import me.anon.grow3.util.newTask
import me.anon.grow3.util.onClick

class NavigationFragment : BaseHostFragment(FragmentNavigationBinding::class)
{
	private val viewBindings by viewBinding<FragmentNavigationBinding>()

	override fun bindUi()
	{
		insets.observe(viewLifecycleOwner) {
			viewBindings.menuView.updatePadding(top = it.top, left = it.left, bottom = it.bottom)
			viewBindings.navigationSettings.onClick {
				newTask<SettingsActivity>()
			}
		}
	}
}
