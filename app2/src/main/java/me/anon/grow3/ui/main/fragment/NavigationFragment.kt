package me.anon.grow3.ui.main.fragment

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import me.anon.grow3.databinding.FragmentNavigationBinding
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.util.onClick

class NavigationFragment : BaseHostFragment(FragmentNavigationBinding::class)
{
	private val viewBindings by viewBinding<FragmentNavigationBinding>()

	override fun bindUi()
	{
		with(insets) {
			viewBindings.menuView.updatePadding(top = top, left = left, bottom = bottom)
			viewBindings.navigationSettings.onClick { it.isVisible = false }
		}
	}
}
