package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.view.plusAssign
import me.anon.grow3.R
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Stage
import me.anon.grow3.databinding.StubViewArrowBinding
import me.anon.grow3.databinding.StubViewStageBinding
import me.anon.grow3.util.*
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

class StageView : HorizontalScrollView
{
	private val stages = mutableListOf<Stage>()
	private val container: StageViewContainer
	private var diary: Diary? = null
	private var crop: Crop? = null
	public var onNewStageClick: () -> Unit = {}

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	{
		container = StageViewContainer(context)
		addView(container, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
	}

	public fun setStages(diary: Diary, crop: Crop)
	{
		this.diary = diary
		this.crop = crop
		this.stages.clear()
		this.stages.addAll(diary.stagesOf(crop))

		layoutStages()
	}

	public fun setStages(diary: Diary)
	{
		this.diary = diary
		this.stages.clear()
		this.stages.addAll(diary.stages())

		layoutStages()
	}

	private fun layoutStages()
	{
		container.removeAllViews()
		for (stageIndex in 0 until stages.size)
		{
			val stage = stages[stageIndex]
			val stageView = StageViewStub(context)
			stageView.setStage(stage)
			container += stageView

			val arrow = ArrowViewStub(context)
			container += arrow

			var stage2Date = LocalDate.now()
			stages.getOrNull(stageIndex + 1)?.let { next ->
				stage2Date = next.date.asLocalDate()
			}

			val stage1Date = stage.date.asLocalDate()
			val days = ChronoUnit.DAYS.between(stage1Date, stage2Date)
			arrow.setLength(days)
		}

		val end = StageViewStub(context)
		end.setOnClickListener {
			onNewStageClick()
		}
		container += end

		requestLayout()
		container.requestLayout()
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

	inner class StageViewContainer : LinearLayout
	{
		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			orientation = HORIZONTAL
			isFillViewport = true
			setPadding(12.dp(this), 0, 12.dp(this), 0)
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
		}

		public fun setStage(stage: Stage?)
		{
			this.stage = stage

			if (stage == null)
			{
				bindings.stageLabel.setText(R.string.today)
				bindings.stageDate.text = LocalDate.now().asFormattedString()
			}
			else
			{
				bindings.stageLabel.text = stage.type.strRes.string(context)
				bindings.stageDate.text = stage.date.asLocalDate().asFormattedString()
				bindings.stageIcon.setImageResource(stage.type.iconRes)
			}
		}
	}

	inner class ArrowViewStub : FrameLayout
	{
		private val bindings: StubViewArrowBinding

		constructor(context: Context) : this(context, null)
		constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
		constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
		{
			bindings = StubViewArrowBinding.inflate(LayoutInflater.from(context), this, true)
		}

		public fun setLength(days: Long)
		{
			bindings.stageLabel.text = R.string.days.string(context, days)
		}

		override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
		{
			val parent = parentView.parentView
			val width = parent.measuredWidth
			val height = parent.measuredHeight - parent.paddingTop - parent.paddingBottom
			val longWidth = context.resources.getBoolean(R.bool.long_width)
			val arrowWidth = longWidth then width / 4 ?: width / 3
			super.onMeasure(
				MeasureSpec.makeMeasureSpec(arrowWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
			)
		}
	}
}
