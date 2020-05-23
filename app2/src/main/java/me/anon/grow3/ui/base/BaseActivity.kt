package me.anon.grow3.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.include_toolbar.view.*
import me.anon.grow3.R

open class BaseActivity(val layoutRes: Int = -1) : AppCompatActivity()
{
	protected var toolbar: Toolbar? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		if (layoutRes != -1) setContentView(layoutRes)
	}

	override fun setContentView(layoutResID: Int)
	{
		super.setContentView(layoutResID)

		findViewById<View?>(R.id.include_toolbar)?.let {
			toolbar = it.toolbar
		}
	}
}
