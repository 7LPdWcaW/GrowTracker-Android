package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.plusAssign
import me.anon.grow3.R
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Stage
import me.anon.grow3.util.inflate
import me.anon.grow3.util.parentView

class StageView : HorizontalScrollView
{
	private val stages = mutableListOf<Stage>()
	private val container: StageViewContainer

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		container = StageViewContainer(context)
		addView(container, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
	}

	public fun setStages(diary: Diary, crop: Crop)
	{
		setStages(diary.stagesOf(crop))
	}

	public fun setStages(stages: List<Stage>)
	{
		this.stages.clear()
		this.stages.addAll(stages)

		layoutStages()
	}

	private fun layoutStages()
	{
		container.removeAllViews()
		(stages + stages).forEach { stage ->
			val stub = StageViewStub(context)
			container += stub
			val arrow = ArrowViewStub(context)
			container += arrow
		}

		requestLayout()
	}

	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int)
	{
		super.onLayout(changed, l, t, r, b)

		if (changed)
		{
			fullScroll(FOCUS_RIGHT)
		}
	}

	override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
	{
		super.onScrollChanged(l, t, oldl, oldt)
		val end = l + measuredWidth
	}

	inner class StageViewContainer : LinearLayout
	{
		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			orientation = HORIZONTAL
			isFillViewport = true
		}
	}

	inner class StageViewStub : ConstraintLayout
	{
		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			inflate<View>(R.layout.stub_view_stage, true)
		}

//		override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
//		{
//			val width = parentView.parentView.measuredWidth
//			super.onMeasure(MeasureSpec.makeMeasureSpec(width / 3, MeasureSpec.EXACTLY), heightMeasureSpec)
////			updatePadding(left = measuredWidth / 3)
//		}
	}

	inner class ArrowViewStub : FrameLayout
	{
		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			inflate<View>(R.layout.stub_view_arrow, true)
		}

		override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
		{
			val width = parentView.parentView.measuredWidth
			val height = parentView.parentView.measuredHeight
			super.onMeasure(MeasureSpec.makeMeasureSpec(width / 3, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
			)
//			updatePadding(left = measuredWidth / 3)
		}
	}
}
