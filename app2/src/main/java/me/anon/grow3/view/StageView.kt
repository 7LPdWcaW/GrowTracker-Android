package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

class StageView : View
{
	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
