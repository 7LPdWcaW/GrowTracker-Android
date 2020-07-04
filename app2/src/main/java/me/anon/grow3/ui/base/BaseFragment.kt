package me.anon.grow3.ui.base

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import me.anon.grow3.util.Injector
import me.anon.grow3.util.component
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseFragment : Fragment
{
	constructor() : super()
	constructor(layoutRes: Int) : super(layoutRes)

	constructor(kClass: KClass<out ViewBinding>)
	{
		this._viewBinder = kClass.java
	}

	private var _viewBinder: Class<out ViewBinding>? = null
	public lateinit var viewBinder: ViewBinding private set

	public fun <T : ViewBinding> viewBinding()
		= lazy(LazyThreadSafetyMode.NONE) {
			viewBinder as T
		}

	open var insets: Rect
		get() = (activity as BaseActivity).insets
		set(value) { (activity as BaseActivity).insets = value }

	abstract val injector: Injector

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		if (_viewBinder == null)
		{
			return super.onCreateView(inflater, container, savedInstanceState)
		}
		else
		{
			viewBinder = _viewBinder!!.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
				.invoke(_viewBinder, inflater, container, false) as ViewBinding
			return viewBinder.root
		}
	}

	override fun onAttach(context: Context)
	{
		super.onAttach(context)
		injector(component)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		Timber.e("from instance %s", savedInstanceState)
		bindArguments(arguments ?: savedInstanceState)
		bindUi()
		bindVm()
	}

	open fun onBackPressed(): Boolean = false
	open fun bindArguments(bundle: Bundle?) {}
	open fun bindUi(){}
	open fun bindVm(){}

	public fun setToolbar(toolbar: Toolbar)
	{
		(activity as BaseActivity).setSupportActionBar(toolbar)
	}
}
