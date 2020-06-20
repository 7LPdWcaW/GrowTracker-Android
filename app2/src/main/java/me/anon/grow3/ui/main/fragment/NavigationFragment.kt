package me.anon.grow3.ui.main.fragment

import android.graphics.Rect
import androidx.core.view.updatePadding
import me.anon.grow3.databinding.FragmentNavigationBinding
import me.anon.grow3.ui.base.BaseHostFragment

class NavigationFragment : BaseHostFragment(FragmentNavigationBinding::class)
{
	private val viewBindings by viewBinding<FragmentNavigationBinding>()

	override var insets: Rect
		get() = super.insets
		set(value)
		{
			super.insets = value
		}

	override fun bindUi()
	{
		requireView().updatePadding(bottom = insets.bottom)
	}
}
