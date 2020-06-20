package me.anon.grow3.ui.base

import androidx.viewbinding.ViewBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.main.activity.MainActivity
import kotlin.reflect.KClass

open class BaseHostFragment(bindings: KClass<out ViewBinding>) : BaseFragment(bindings)
{
	override fun onBackPressed(): Boolean = false
	protected fun activity(): MainActivity = requireActivity() as MainActivity

	override val inject: (ApplicationComponent) -> Unit = {}

	override fun bindUi(){}
	override fun bindVm(){}
}
