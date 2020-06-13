package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import me.anon.grow3.databinding.FragmentViewDiaryBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.applyWindowInsets
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick

class ViewDiaryFragment : BaseFragment(FragmentViewDiaryBinding::class.java)
{
	companion object
	{
		public const val EXTRA_DIARY_ID = "diary.id"
	}

	override val inject: (ApplicationComponent) -> Unit = {}
	private val viewBindings by lazy { binding<FragmentViewDiaryBinding>() }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)
		setToolbar(viewBindings.toolbar)
		applyWindowInsets()
	}

	override fun bindArguments(bundle: Bundle?)
	{
		requireActivity().title = "Diary"
	}

	override fun bindUi()
	{
		viewBindings.crop1.onClick {
			navigateTo<MainActivity> {
				putExtras(bundleOf(MainActivity.EXTRA_NAVIGATE to MainActivity.NAVIGATE_TO_CROPS))
//				putExtras(bundleOf(MainActivity.EXTRA_DIARY_ID to diaryId))
			}
		}
	}

	override fun bindVm()
	{

	}
}
