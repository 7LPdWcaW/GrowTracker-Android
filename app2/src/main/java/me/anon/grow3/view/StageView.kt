package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.core.view.*
import com.google.android.flexbox.FlexDirection.ROW
import com.google.android.flexbox.FlexboxLayout
import me.anon.grow3.R
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Stage
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.databinding.StubViewArrowBinding
import me.anon.grow3.databinding.StubViewStageBinding
import me.anon.grow3.util.*
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

class StageView : HorizontalScrollView
{
	private val stages = mutableListOf<Stage>()
	private val container: StageViewContainer
	private var diary: Diary? = null
	private var crop: Crop? = null
	public var onNewStageClick: (View) -> Unit = {}
	public var onStageClick: (StageChange) -> Unit = {}

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		container = StageViewContainer(context)
		addView(container, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
	}

	public fun setStages(diary: Diary, crop: Crop? = null)
	{
		this.diary = diary
		this.crop = crop
		this.stages.clear()
		this.stages.addAll(crop?.run { diary.stagesOf(this) } ?: diary.stages())

		layoutStages()
	}

	private fun layoutStages()
	{
		container.removeAllViews()
		container.removeAllViewsInLayout()
		for (stageIndex in 0 until stages.size)
		{
			val stage = stages[stageIndex]
			val stageView = StageViewStub(context)
			stageView.setStage(stage)
			stageView.onClick {
				onStageClick(stage)
				smoothScrollTo(it.x.toInt(), 0)
			}
			container += stageView

			val arrow = ArrowViewStub(context)
			arrow.single = stages.size == 1
			arrow.layoutParams = FlexboxLayout.LayoutParams(
				WRAP_CONTENT,
				MATCH_PARENT
			).apply {
				flexGrow = 1f
				minWidth = 112.dp
			}

			container += arrow

			var stage2Date = ZonedDateTime.now()
			stages.getOrNull(stageIndex + 1)?.let { next ->
				stage2Date = next.date.asDateTime()
			}

			val stage1Date = stage.date.asDateTime()
			val days = ChronoUnit.DAYS.between(stage1Date, stage2Date)
			arrow.setLength(days)
		}

		val end = StageViewStub(context)
		end.onClick {
			// todo: center clicked view in the scroll container
			smoothScrollTo(it.x.toInt(), 0)
			onNewStageClick(it)
		}
		container += end

		requestLayout()
		container.requestLayout()
		invalidate()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		val width = MeasureSpec.getSize(widthMeasureSpec)

		if (container.childCount == 3)
		{

		}
		else
		{
			container.childViews.forEach {
				(it as? ArrowViewStub)?.updateLayoutParams {
					this.width = 500//width / 3
				}
			}
		}
	}

	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int)
	{
		super.onLayout(changed, l, t, r, b)

		if (changed)
		{
			fullScroll(FOCUS_RIGHT)
		}
	}

	override fun onTouchEvent(ev: MotionEvent?): Boolean
	{
		ev?.let { event ->
			if (event.actionMasked == MotionEvent.ACTION_UP)
			{
				// scroll to closest stage view stub
			}
		}

		return super.onTouchEvent(ev)
	}

	inner class StageViewContainer : FlexboxLayout
	{
		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			flexDirection = ROW
			isFillViewport = true
			//setPadding(12.dp(this), 0, 12.dp(this), 0)
		}

		override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int)
		{
			super.onLayout(changed, l, t, r, b)
		}
	}

	inner class StageViewStub : FrameLayout
	{
		private val bindings: StubViewStageBinding
		private var stage: Stage? = null

		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			bindings = StubViewStageBinding.inflate(LayoutInflater.from(context), this, true)
			setStage(null)
			isClickable = true
			isFocusable = true
		}

		override fun setOnClickListener(l: OnClickListener?)
		{
			bindings.stageIcon.onClick {
				smoothScrollTo(it.x.toInt(), 0)
				l?.onClick(this@StageViewStub)
			}
		}

		public fun setStage(stage: Stage?)
		{
			this.stage = stage

			if (stage == null)
			{
				bindings.stageLabel.setText(R.string.today)
				bindings.stageDate.text = ZonedDateTime.now().formatDate()
				bindings.stageIcon.setImageDrawable(R.drawable.ic_add.drawable(context, tint = R.attr.textOnSurface.resColor(context)))
			}
			else
			{
				bindings.stageLabel.text = stage.type.strRes.string(context)
				bindings.stageDate.text = stage.date.asDateTime().formatDate()
				bindings.stageIcon.setImageResource(stage.type.iconRes)
			}
		}
	}

	inner class ArrowViewStub : FrameLayout
	{
		public var single = false
		private val bindings: StubViewArrowBinding

		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			bindings = StubViewArrowBinding.inflate(LayoutInflater.from(context), this, true)
		}

		public fun setLength(days: Long)
		{
			bindings.stageLabel.isVisible = days > 0
			bindings.stageLabel.text = R.string.days.string(context, days)
		}
	}
}
