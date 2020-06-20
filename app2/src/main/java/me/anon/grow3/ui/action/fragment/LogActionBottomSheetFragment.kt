package me.anon.grow3.ui.action.fragment

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentActionLogBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.util.*
import timber.log.Timber
import kotlin.math.abs

class LogActionBottomSheetFragment : BaseFragment(FragmentActionLogBinding::class)
{
	override val inject: Injector = {}
	private val viewBindings by viewBinding<FragmentActionLogBinding>()

	private val layoutSheetBehavior by lazy { BottomSheetBehavior.from(requireView().parentViewById<View>(R.id.bottom_sheet)) }
	private val sheetListener = object : BottomSheetBehavior.BottomSheetCallback()
	{
		override fun onSlide(bottomSheet: View, slideOffset: Float)
		{
			if (bottomSheet.top < insets.top)
			{
				val elevation = slideOffset * 8f
				Timber.e("$elevation")
				viewBindings.toolbarLayout.elevation = elevation
				viewBindings.toolbarLayout.setBackgroundColor(R.attr.colorSurface.resColor(requireContext()))

				viewBindings.toolbarLayout.updateMargin(top = abs(bottomSheet.top - insets.top))
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
		layoutSheetBehavior.isGestureInsetBottomIgnored = false
		layoutSheetBehavior.isFitToContents = false
		layoutSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

		requireView().parentViewById<View>(R.id.bottom_sheet).post {
			layoutSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
			layoutSheetBehavior.isHideable = false

			viewBindings.toolbarLayout.afterMeasured {
				layoutSheetBehavior.setPeekHeight(measuredHeight + 12.dp(context) + insets.bottom, false)
			}
		}

		viewBindings.toolbar.setNavigationOnClickListener {
			requireActivity().promptExit {
				layoutSheetBehavior.isHideable = true
				layoutSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
			}
		}

		layoutSheetBehavior.addBottomSheetCallback(sheetListener)
	}

	override fun onDestroy()
	{
		super.onDestroy()
		layoutSheetBehavior.removeBottomSheetCallback(sheetListener)
	}
}
