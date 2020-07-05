package me.anon.grow3.ui.base

import androidx.viewbinding.ViewBinding
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.Injector
import kotlin.reflect.KClass

open class BaseHostFragment(bindings: KClass<out ViewBinding>) : BaseFragment(bindings)
{
	override fun onBackPressed(): Boolean = false
	protected fun activity(): MainActivity = requireActivity() as MainActivity

	override val injector: Injector = {}

	override fun bindUi(){}
	override fun bindVm(){}
}
