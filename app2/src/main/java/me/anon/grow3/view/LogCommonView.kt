package me.anon.grow3.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.databinding.ViewLogCommonBinding
import me.anon.grow3.util.*

class LogCommonView : ConstraintLayout
{
	private lateinit var bindings: ViewLogCommonBinding

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		bindings = ViewLogCommonBinding.inflate(LayoutInflater.from(context), this, true)
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
		bindings.cropSelectView.setDiary(diary)

		bindings.date.editText!!.text = log.date.asDateTime().asDisplayString().asEditable()
		bindings.notes.editText!!.text = log.notes.asEditable()
	}

	public fun saveTo(log: Log): Log
	{
		log.cropIds = bindings.cropSelectView.selectedCrops.toList()
		log.notes = bindings.notes.editText!!.text.toString()
		log.date = bindings.date.editText!!.text.toString().fromDisplayString().asApiString()
		return log
	}
}
