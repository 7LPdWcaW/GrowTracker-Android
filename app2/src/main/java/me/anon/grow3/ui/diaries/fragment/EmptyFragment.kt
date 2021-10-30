package me.anon.grow3.ui.diaries.fragment

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import me.anon.grow3.databinding.FragmentEmptyDiariesBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.util.Injector
import me.anon.grow3.util.component
import me.anon.grow3.util.newTask
import me.anon.grow3.util.onClick

class EmptyFragment : BaseFragment(FragmentEmptyDiariesBinding::class)
{
	override val injector: Injector = {}
	private val viewBindings by viewBinding<FragmentEmptyDiariesBinding>()

	override fun bindVm()
	{
		lifecycleScope.launchWhenCreated {
			if (component.diariesRepo().getDiaryCount(false) == 0)
			{
				viewBindings.emptyText.text = "You have no diaries"
				viewBindings.emptyText.isVisible = true
			}
		}
	}

	override fun bindUi()
	{
		viewBindings.newDiary.onClick {
			newTask<DiaryActivity>()
		}
	}
}
