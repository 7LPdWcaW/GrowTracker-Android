package me.anon.grow3.ui.diaries.fragment

import me.anon.grow3.databinding.FragmentEmptyDiariesBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.util.newTask
import me.anon.grow3.util.onClick

class EmptyFragment : BaseFragment(FragmentEmptyDiariesBinding::class)
{
	override val inject: (ApplicationComponent) -> Unit = {}
	private val viewBindings by lazy { binding<FragmentEmptyDiariesBinding>() }

	override fun bindUi()
	{
		viewBindings.newDiary.onClick {
			newTask<DiaryActivity>()
		}
	}

	override fun bindVm()
	{

	}
}
