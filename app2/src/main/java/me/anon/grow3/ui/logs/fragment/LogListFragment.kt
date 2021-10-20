package me.anon.grow3.ui.logs.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import me.anon.grow3.data.model.*
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.logs.view.LogDateSeparator
import me.anon.grow3.ui.logs.viewmodel.LogListViewModel
import me.anon.grow3.util.*
import javax.inject.Inject

class LogListFragment : CardListFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: LogListViewModel.Factory
	private val viewModel: LogListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		lifecycleScope.launchWhenCreated {
			viewModel.state
				.collectLatest { state ->
					if (state !is LogListViewModel.UiResult.Loaded) return@collectLatest

					val diary = state.diary
					val logs = state.logs
					val crop = state.crops?.firstOrNull()

					val title = crop?.name
						?: diary.name
					requireActivity().title = "$title logs"

					viewAdapter.newStack {
						val group = logs
							.groupBy { log ->
								log.date.asDate()
							}
							.toSortedMap { o1, o2 ->
								-o1.compareTo(o2)
							}

						group.forEach { (date, logs) ->
							add(LogDateSeparator(date.asDisplayString(), logs.last().date.ago(false) + " • " + diary.stageWhen(logs.last()).longString()))
							addAll(logs.reversed().map { it.asCard(diary) })
						}
					}
				}
		}
	}
}
