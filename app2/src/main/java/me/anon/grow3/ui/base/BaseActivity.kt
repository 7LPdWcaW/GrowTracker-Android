package me.anon.grow3.ui.base

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import me.anon.grow3.R
import me.anon.grow3.databinding.IncludeToolbarBinding
import me.anon.grow3.util.Injector
import me.anon.grow3.util.component
import kotlin.reflect.KClass

open class BaseActivity : AppCompatActivity
{
	constructor() : super()

	constructor(viewBinder: KClass<out ViewBinding>)
	{
		this._viewBinder = viewBinder.java
	}

	open val injector: Injector = {}

	private var layoutRes = -1
	private var _viewBinder: Class<out ViewBinding>? = null
	private lateinit var viewBinder: ViewBinding private set

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

	private val _insets: MutableLiveData<Rect> = MutableLiveData(Rect())
	public val insets: LiveData<Rect> = _insets

	@ColorInt
	public var statusBarColor: Int = -1
		set(value) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window.statusBarColor = value
			field = value
		}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		injector(component)

		super.onCreate(savedInstanceState)

		if (_viewBinder != null)
		{
			viewBinder = _viewBinder!!.getDeclaredMethod("inflate", LayoutInflater::class.java)
				.invoke(_viewBinder, layoutInflater) as ViewBinding
			setContentView(viewBinder.root)

			viewBinder.root.setOnApplyWindowInsetsListener { v, i ->
				v.onApplyWindowInsets(i).also {
					_insets.postValue(Rect(i.systemWindowInsetLeft, i.systemWindowInsetTop, i.systemWindowInsetRight, i.systemWindowInsetBottom))
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
			val bind = IncludeToolbarBinding.bind(it)
			toolbar = bind.toolbar
			statusBarColor = bind.toolbar.background.toBitmap(1, 1, Bitmap.Config.RGB_565)[0,0]
		}
	}

	open fun bindUi(){}
	open fun bindVm(){}

	open fun onFragmentAdded(fragment: Fragment){}
	open fun onFragmentRemoved(fragment: Fragment){}
}
