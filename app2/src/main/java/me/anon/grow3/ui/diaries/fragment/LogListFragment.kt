package me.anon.grow3.ui.diaries.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.diaries.view.LogCard
import me.anon.grow3.ui.diaries.view.LogDateSeparator
import me.anon.grow3.ui.diaries.viewmodel.LogListViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.asDateTime
import me.anon.grow3.util.formatDate
import me.anon.grow3.util.states.asSuccess
import javax.inject.Inject

class LogListFragment : CardListFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: LogListViewModel.Factory
	private val viewModel: LogListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindUi()
	{
		super.bindUi()
	}

	override fun bindVm()
	{
		viewModel.logs.observe(viewLifecycleOwner) { logs ->
			val diary = viewModel.diary.value!!.asSuccess()
			viewAdapter.newStack {
				val group = logs.groupBy { log ->
					log.date.asDateTime().formatDate()
				}

				group.forEach { (date, logs) ->
					add(LogDateSeparator(date))

					logs.forEach { log ->
						add(LogCard(diary, log))
					}
				}
			}
		}
	}
}
