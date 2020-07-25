package me.anon.grow3.ui.setup.fragment

import android.os.Bundle
import android.view.View
import com.github.appintro.SlideBackgroundColorHolder
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentIntroSlideImportBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.Injector
import me.anon.grow3.util.color

class ImportSlideFragment : BaseFragment(FragmentIntroSlideImportBinding::class), SlideBackgroundColorHolder
{
	override val injector: Injector = {}
	private val viewBinding by viewBinding<FragmentIntroSlideImportBinding>()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)
	}

	override val defaultBackgroundColor: Int
		get() = R.color.colorSecondaryAlt.color(requireContext())

	override fun setBackgroundColor(backgroundColor: Int)
	{
		viewBinding.main.setBackgroundColor(backgroundColor)
	}
}
