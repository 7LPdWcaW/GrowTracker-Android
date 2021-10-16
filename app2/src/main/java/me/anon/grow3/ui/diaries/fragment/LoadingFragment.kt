package me.anon.grow3.ui.diaries.fragment

import me.anon.grow3.databinding.FragmentLoadingBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.Injector

class LoadingFragment : BaseFragment(FragmentLoadingBinding::class)
{
	override val injector: Injector = {}
	internal val viewBindings by viewBinding<FragmentLoadingBinding>()
}
