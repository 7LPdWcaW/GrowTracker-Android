package me.anon.grow3.ui.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.util.component

abstract class BaseFragment(layoutRes: Int) : Fragment(layoutRes)
{
	abstract val inject: (ApplicationComponent) -> Unit

	override fun onAttach(context: Context)
	{
		super.onAttach(context)
		inject(component)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		bindUi()
		bindVm()
	}

	abstract fun bindUi()
	abstract fun bindVm()
}
