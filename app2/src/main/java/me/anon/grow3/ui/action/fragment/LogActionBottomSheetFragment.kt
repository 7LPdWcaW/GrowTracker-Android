package me.anon.grow3.ui.action.fragment

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentActionLogBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.*
import kotlin.math.abs

class LogActionBottomSheetFragment : BaseFragment(FragmentActionLogBinding::class)
{
	override val injector: Injector = {}
	private val viewBindings by viewBinding<FragmentActionLogBinding>()

	private val layoutSheetBehavior by lazy { BottomSheetBehavior.from(requireView().parentViewById<View>(R.id.bottom_sheet)) }
	private val sheetListener = object : BottomSheetBehavior.BottomSheetCallback()
	{
		override fun onSlide(bottomSheet: View, slideOffset: Float)
		{
			if (bottomSheet.top < insets.value?.top ?: 0)
			{
				val elevation = slideOffset * 8f
				viewBindings.toolbarLayout.elevation = elevation
				viewBindings.toolbarLayout.setBackgroundColor(R.attr.colorSurface.resColor(requireContext()))

				viewBindings.toolbarLayout.updateMargin(top = abs(bottomSheet.top - (insets.value?.top ?: 0)))
			}
			else
			{
				viewBindings.toolbarLayout.updateMargin(top = 12.dp(requireContext()))
				viewBindings.toolbarLayout.elevation = 0f
				viewBindings.toolbarLayout.setBackgroundColor(android.R.color.transparent.color(requireContext()))
			}
		}

		override fun onStateChanged(bottomSheet: View, newState: Int){}
	}

	override fun bindUi()
	{
		setToolbar(viewBindings.toolbar)
		viewBindings.toolbar.setNavigationOnClickListener {
			requireActivity().promptExit {
				layoutSheetBehavior.isHideable = true
				layoutSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
			}
		}

		layoutSheetBehavior.isGestureInsetBottomIgnored = false
		layoutSheetBehavior.isFitToContents = false
		layoutSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

		requireView().parentViewById<View>(R.id.bottom_sheet).post {
			layoutSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
			layoutSheetBehavior.isHideable = false

			viewBindings.toolbarLayout.afterMeasured {
				layoutSheetBehavior.setPeekHeight(measuredHeight + 12.dp(context) + (insets.value?.bottom ?: 0), false)
			}
		}

		layoutSheetBehavior.addBottomSheetCallback(sheetListener)
	}

	override fun onBackPressed(): Boolean
	{
		if (layoutSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED)
		{
			requireActivity().promptExit {
				layoutSheetBehavior.isHideable = true
				layoutSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
			}
			return true
		}
		else if (layoutSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
		{
			layoutSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
			return true
		}

		return false
	}

	override fun onDestroy()
	{
		super.onDestroy()
		layoutSheetBehavior.removeBottomSheetCallback(sheetListener)
	}
}
