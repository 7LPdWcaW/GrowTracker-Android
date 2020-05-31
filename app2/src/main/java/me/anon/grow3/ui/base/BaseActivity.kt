package me.anon.grow3.ui.base

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import kotlinx.android.synthetic.main.include_toolbar.view.*
import me.anon.grow3.R

open class BaseActivity(val layoutRes: Int = -1) : AppCompatActivity()
{
	protected var toolbar: Toolbar? = null
		set(value)
		{
			setSupportActionBar(value)
			field = value
		}

	@ColorInt
	protected var statusBarColor: Int = -1
		set(value) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			{
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
				window.statusBarColor = value
			}
			field = value
		}

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
			statusBarColor = it.toolbar.background.toBitmap(1, 1, Bitmap.Config.RGB_565)[0,0]
		}
	}
}
