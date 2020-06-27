package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.flexbox.FlexLine
import com.google.android.flexbox.FlexboxLayout

class FlexBoxLayout : FlexboxLayout
{
	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	public var onFlexLine: FlexboxLayout.(index: Int, flexLine: FlexLine?) -> Unit = { _, _ -> }

	override fun onNewFlexLineAdded(flexLine: FlexLine?)
	{
		super.onNewFlexLineAdded(flexLine)
		onFlexLine(this, flexLines.size, flexLine)
	}
}
