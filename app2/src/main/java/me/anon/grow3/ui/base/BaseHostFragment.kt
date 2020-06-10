package me.anon.grow3.ui.base

import androidx.fragment.app.Fragment
import me.anon.grow3.ui.main.activity.MainActivity

open class BaseHostFragment(layoutId: Int) : Fragment(layoutId)
{
	open fun onBackPressed(): Boolean = false
	protected fun activity(): MainActivity = requireActivity() as MainActivity
}
