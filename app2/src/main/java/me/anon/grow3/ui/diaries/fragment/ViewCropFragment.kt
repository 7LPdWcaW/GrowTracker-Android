package me.anon.grow3.ui.diaries.fragment

import androidx.core.view.updatePadding
import me.anon.grow3.databinding.FragmentViewCropBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.Injector
import me.anon.grow3.util.dp
import me.anon.grow3.util.updateMargin

class ViewCropFragment : BaseFragment(FragmentViewCropBinding::class)
{
	override val injector: Injector = {}
	private val viewBindings by viewBinding<FragmentViewCropBinding>()

	override fun bindUi()
	{
		with (insets) {
			viewBindings.includeToolbar.toolbar.updateMargin(left, top, right)
			viewBindings.content.updatePadding(left, right = right, bottom = bottom + 72.dp(requireContext()))
		}
	}
}
