package me.anon.grow3.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.databinding.ViewLogCommonBinding
import me.anon.grow3.util.*

class LogCommonView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs)
{
	private val bindings: ViewLogCommonBinding

	init {
		bindings = ViewLogCommonBinding.inflate(LayoutInflater.from(context), this, true)
	}

	private var diary: Diary? = null
	private var log: Log? = null
	public var dateChangePrompt: () -> Unit = {}
	public var cropIds: HashSet<String> = hashSetOf()
		set(value) {
			field = value

			bindings.cropSelectView.selectedCrops = value
			if (value.isEmpty())
			{
				bindings.cropSelectView.selectAll = true
			}
		}
	public var cropSelectViewVisible = true
		set(value) {
			field = value
			bindings.cropSelectView.isVisible = value
		}

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

		bindings.cropSelectView.setDiary(diary!!)

		bindings.date.editText!!.text = log!!.date.asDateTime().asDisplayString().asEditable()
		bindings.notes.editText!!.text = log!!.notes.asEditable()

		bindings.date.editText!!.onFocus {
			dateChangePrompt()
		}

		bindings.notes.editText!!.onFocusLoss {
			log?.notes = it.text.toString()
		}
	}

	override fun onSaveInstanceState(): Parcelable?
	{
		val state = Bundle()
		state.putParcelable("super", super.onSaveInstanceState())

		return state
	}

	override fun onRestoreInstanceState(state: Parcelable?)
	{
		val state = state as Bundle
		super.onRestoreInstanceState(state.getParcelable("super"))
	}

	public fun setLog(diary: Diary, log: Log)
	{
		this.diary = diary
		this.log = log
		requestLayout()
		populate()
	}

	public fun saveTo(log: Log): Log
	{
		log.cropIds = bindings.cropSelectView.selectedCrops.toList()
		log.notes = bindings.notes.editText!!.text.toString()
		log.date = bindings.date.editText!!.text.toString().fromDisplayString().asApiString()
		return log
	}
}
