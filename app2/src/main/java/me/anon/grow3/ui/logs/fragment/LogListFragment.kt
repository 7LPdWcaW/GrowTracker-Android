package me.anon.grow3.ui.logs.fragment

import androidx.fragment.app.viewModels
import com.freelapp.flowlifecycleobserver.collectWhileStarted
import me.anon.grow3.data.model.*
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.logs.view.LogDateSeparator
import me.anon.grow3.ui.logs.viewmodel.LogListViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.asDate
import me.anon.grow3.util.asDisplayString
import javax.inject.Inject

class LogListFragment : CardListFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: LogListViewModel.Factory
	private val viewModel: LogListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.state
			.collectWhileStarted(viewLifecycleOwner) { state ->
				if (state !is LogListViewModel.UiResult.Loaded) return@collectWhileStarted

				val diary = state.diary
				val logs = state.logs
				val crop = state.crops?.firstOrNull()

				val title = crop?.name ?: diary.name
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
						add(LogDateSeparator(date.asDisplayString()))
						addAll(logs.reversed().map { it.asCard(diary) })
					}
				}
			}
	}
}
