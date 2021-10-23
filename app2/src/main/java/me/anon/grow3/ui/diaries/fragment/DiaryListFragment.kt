package me.anon.grow3.ui.diaries.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import me.anon.grow3.databinding.FragmentDiariesBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.diaries.adapter.DiaryListAdapter
import me.anon.grow3.ui.diaries.viewmodel.DiaryListViewModel
import me.anon.grow3.util.*
import javax.inject.Inject

class DiaryListFragment : BaseFragment(FragmentDiariesBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryListViewModel.Factory
	private val viewModel: DiaryListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentDiariesBinding>()
	private val adapter = DiaryListAdapter()

	override fun bindUi()
	{
		adapter.onItemClick = { item ->
			navigateTo<ViewDiaryFragment> {
				bundleOf(EXTRA_DIARY_ID to item.id)
			}
		}

		viewBindings.newDiary.onClick {
			newTaskForResult<DiaryActivity>()
		}

		viewBindings.recyclerView.adapter = adapter
		viewBindings.recyclerView.layoutManager = LinearLayoutManager(requireContext())
	}

	override fun bindVm()
	{
		lifecycleScope.launchWhenCreated {
			viewModel.state
				.collectLatest { state ->
					when (state)
					{
						is DiaryListViewModel.UiState.Loaded -> {
							adapter.items = state.diaries
							adapter.notifyDataSetChanged()
						}
					}
				}
		}
	}
}
