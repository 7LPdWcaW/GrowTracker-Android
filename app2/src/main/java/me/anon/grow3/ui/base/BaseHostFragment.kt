package me.anon.grow3.ui.base

import androidx.viewbinding.ViewBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.main.activity.MainActivity

open class BaseHostFragment(bindings: Class<out ViewBinding>) : BaseFragment(bindings)
{
	open fun onBackPressed(): Boolean = false
	protected fun activity(): MainActivity = requireActivity() as MainActivity

	override val inject: (ApplicationComponent) -> Unit = {}

	override fun bindUi(){}
	override fun bindVm(){}
}
