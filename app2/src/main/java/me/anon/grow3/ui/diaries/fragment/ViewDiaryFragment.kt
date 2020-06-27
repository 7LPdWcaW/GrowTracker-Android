package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.core.view.updatePadding
import me.anon.grow3.databinding.FragmentViewDiaryBinding
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.*

class ViewDiaryFragment : BaseFragment(FragmentViewDiaryBinding::class)
{
	override val injector: Injector = { it.inject(this) }
	private val viewBindings by viewBinding<FragmentViewDiaryBinding>()

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		setToolbar(viewBindings.toolbar)

		with (insets) {
			viewBindings.menuFab.updateMargin(bottom = bottom + 16.dp(requireContext()), top = top + 16.dp(requireContext()))
			viewBindings.sheet.updatePadding(left, top, right, bottom)
			viewBindings.toolbar.updateMargin(left, top, right)
			viewBindings.content.updatePadding(left, right = right, bottom = bottom + 72.dp(requireContext()))
		}
	}

	override fun onBackPressed(): Boolean
		= viewBindings.menuFab.isExpanded.also { viewBindings.menuFab.isExpanded = false }

	override fun bindArguments(bundle: Bundle?)
	{
		viewBindings.collapsingToolbarLayout.title = "Diary Name"
		viewBindings.collapsingToolbarLayout.subtitle = "10a/20b/30c/100"
	}

	override fun bindUi()
	{
		viewBindings.menuFab.setOnClickListener {
			viewBindings.menuFab.isExpanded = !viewBindings.menuFab.isExpanded
			navigationPager?.isUserInputEnabled = !viewBindings.menuFab.isExpanded
		}

		viewBindings.sheet.setOnClickListener {
			viewBindings.menuFab.isExpanded = false
			navigationPager?.isUserInputEnabled = true
		}

		viewBindings.menuAction1.onClick {
			viewBindings.menuFab.isExpanded = false
			navigationPager?.isUserInputEnabled = true
			navigateTo<LogActionBottomSheetFragment>(true)
		}
	}

	override fun bindVm()
	{

	}
}
