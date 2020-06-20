package me.anon.grow3.ui.base

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import androidx.viewbinding.ViewBinding
import kotlinx.android.synthetic.main.include_toolbar.view.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.util.component
import kotlin.reflect.KClass

open class BaseActivity : AppCompatActivity
{
	constructor() : super()

	constructor(viewBinder: KClass<out ViewBinding>)
	{
		this._viewBinder = viewBinder.java
	}

	open val inject: (ApplicationComponent) -> Unit = {}

	private var layoutRes = -1
	private var _viewBinder: Class<out ViewBinding>? = null
	public lateinit var viewBinder: ViewBinding private set

	public fun <T : ViewBinding> viewBinding()
		= lazy(LazyThreadSafetyMode.NONE) {
			viewBinder as T
		}

	protected var toolbar: Toolbar? = null
		set(value)
		{
			setSupportActionBar(value)
			field = value
		}

	open var insets: Rect = Rect()

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
		inject(component)

		super.onCreate(savedInstanceState)

		if (_viewBinder != null)
		{
			viewBinder = _viewBinder!!.getDeclaredMethod("inflate", LayoutInflater::class.java)
				.invoke(_viewBinder, layoutInflater) as ViewBinding
			setContentView(viewBinder.root)

			viewBinder.root.setOnApplyWindowInsetsListener { v, i ->
				v.onApplyWindowInsets(i).also {
					insets = Rect(i.systemWindowInsetLeft, i.systemWindowInsetTop, i.systemWindowInsetRight, i.systemWindowInsetBottom)
				}
				i.consumeSystemWindowInsets()
			}
		}
	}

	override fun onPostCreate(savedInstanceState: Bundle?)
	{
		super.onPostCreate(savedInstanceState)
		bindUi()
		bindVm()
	}

	override fun setContentView(view: View)
	{
		super.setContentView(view)

		findViewById<View?>(R.id.include_toolbar)?.let {
			toolbar = it.toolbar
			statusBarColor = it.toolbar.background.toBitmap(1, 1, Bitmap.Config.RGB_565)[0,0]
		}
	}

	open fun bindUi(){}
	open fun bindVm(){}
}
