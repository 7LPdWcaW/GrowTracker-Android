package me.anon.grow3.ui.action.fragment

import android.view.View
import androidx.core.graphics.plus
import androidx.core.view.updatePadding
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.anon.grow3.R
import me.anon.grow3.util.*
import java.lang.Math.abs

class LogActionBottomSheetFragment : LogActionFragment()
{
	override val injector: Injector = { it.inject(this) }

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

		override fun onStateChanged(bottomSheet: View, newState: Int)
		{
			when (newState)
			{
				BottomSheetBehavior.STATE_COLLAPSED -> {
					viewBindings.logContent.visibility = View.INVISIBLE
				}
				else -> viewBindings.logContent.visibility = View.VISIBLE
			}
		}
	}

	override fun bindUi()
	{
		super.bindUi()
		viewBindings.toolbar.setNavigationOnClickListener {
			requireActivity().promptExit {
				finish()
			}
		}

		insets.observe(viewLifecycleOwner) { insets ->
			viewBindings.actionDone.updateMargin(insets + 16.dp)
			viewBindings.logContent.updatePadding(bottom = insets.bottom)
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

	override fun finish()
	{
		layoutSheetBehavior.isHideable = true
		layoutSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
		super.finish()
	}

	override fun onBackPressed(): Boolean
	{
		when (layoutSheetBehavior.state)
		{
			BottomSheetBehavior.STATE_COLLAPSED -> {
				requireActivity().promptExit {
					finish()
				}
				return true
			}

			BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.STATE_HALF_EXPANDED -> {
				layoutSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
				return true
			}
		}

		return false
	}

	override fun onDestroy()
	{
		super.onDestroy()
		layoutSheetBehavior.removeBottomSheetCallback(sheetListener)
	}
}
