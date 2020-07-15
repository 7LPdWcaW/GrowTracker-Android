package me.anon.grow3.ui.logs.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.data.model.*
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.logs.view.*
import me.anon.grow3.ui.logs.viewmodel.LogListViewModel
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
				val group =
					logs.groupBy { log ->
						log.date.asDateTime().formatDate()
					}
					.toSortedMap(Comparator { o1, o2 ->
						-o1.compareTo(o2)
					})

				group.forEach { (date, logs) ->
					add(LogDateSeparator(date))

					logs.forEach { log ->
						add(when (log)
						{
							is Environment -> EnvironmentLogCard(diary, log)
							is Harvest -> HarvestLogCard(diary, log)
							is Maintenance -> MaintenanceLogCard(diary, log)
							is Pesticide -> PesticideLogCard(diary, log)
							is Photo -> PhotoLogCard(diary, log)
							is StageChange -> StageChangeLogCard(diary, log)
							is Transplant -> TransplantLogCard(diary, log)
							is Water -> WaterLogCard(diary, log)
							else -> throw IllegalArgumentException("Could not handle $log")
						})
					}
				}
			}
		}
	}
}
