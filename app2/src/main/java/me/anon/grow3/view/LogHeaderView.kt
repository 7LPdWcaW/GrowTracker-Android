package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.databinding.StubCardHeaderBinding
import me.anon.grow3.util.asDateTime
import me.anon.grow3.util.formatTime
import me.anon.grow3.util.string
import me.anon.grow3.util.whenNotNull

class LogHeaderView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs)
{
	private val bindings: StubCardHeaderBinding

	init {
		bindings = StubCardHeaderBinding.inflate(LayoutInflater.from(context), this, true)
	}

	private var diary: Diary? = null
	private var log: Log? = null

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		populate()
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		populate()
	}

	private fun populate()
	{
		diary ?: return
		log ?: return

		whenNotNull(log, diary) { log, diary ->
			bindings.header.text = if (log.typeRes != -1) log.typeRes.string() else log.action
			bindings.date.text = "${log.date.asDateTime().formatTime()}"
		}
	}

	public fun setLog(diary: Diary, log: Log)
	{
		this.diary = diary
		this.log = log
		requestLayout()
		populate()
	}
}
