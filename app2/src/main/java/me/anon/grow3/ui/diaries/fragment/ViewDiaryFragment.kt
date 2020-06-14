package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import me.anon.grow3.databinding.FragmentViewDiaryBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.*

class ViewDiaryFragment : BaseFragment(FragmentViewDiaryBinding::class)
{
	companion object
	{
		public const val EXTRA_DIARY_ID = "diary.id"
	}

	override val inject: (ApplicationComponent) -> Unit = {}
	private val viewBindings by lazy { binding<FragmentViewDiaryBinding>() }

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		setToolbar(viewBindings.toolbar)

		applyWindowInsets(
			viewBindings.menuFab,
			viewBindings.sheet,
			viewBindings.toolbar
		) { v, l, t, r, b ->
			when (v)
			{
				viewBindings.menuFab -> v.updateMarginRelative(bottom = b)
				viewBindings.sheet -> v.updatePadding(l, t, r, b)
				viewBindings.toolbar -> v.updateMargin(l, t, r)
			}
		}
	}

	override fun bindArguments(bundle: Bundle?)
	{
		viewBindings.collapsingToolbarLayout.title = "Diary Name"
		viewBindings.collapsingToolbarLayout.subtitle = "10a/20b/30c/100"
	}

	override fun bindUi()
	{
		viewBindings.crop1.onClick {
			navigateTo<MainActivity> {
				putExtras(bundleOf(MainActivity.EXTRA_NAVIGATE to MainActivity.NAVIGATE_TO_CROPS))
//				putExtras(bundleOf(MainActivity.EXTRA_DIARY_ID to diaryId))
			}
		}

		viewBindings.menuFab.setOnClickListener {
			viewBindings.menuFab.isExpanded = !viewBindings.menuFab.isExpanded
			navigationPager?.isUserInputEnabled = !viewBindings.menuFab.isExpanded
		}
		viewBindings.sheet.setOnClickListener {
			viewBindings.menuFab.isExpanded = false
			navigationPager?.isUserInputEnabled = true
		}
	}

	override fun bindVm()
	{

	}
}
