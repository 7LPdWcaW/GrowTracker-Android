package me.anon.grow3.ui.base

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import me.anon.grow3.util.Injector
import me.anon.grow3.util.component
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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

	public fun <T : ViewBinding> viewBinding() = Retriever<T>()
	inner class Retriever<T : ViewBinding>
	{
		operator fun getValue(thisRef: BaseFragment, property: KProperty<*>): T = thisRef.viewBinder as T
	}

	open val insets: LiveData<Rect>
		get() = (activity as BaseActivity).insets

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
		(activity as? BaseActivity)?.onFragmentAdded(this)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

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

	override fun onDestroy()
	{
		(activity as? BaseActivity)?.onFragmentRemoved(this)
		super.onDestroy()
	}
}
