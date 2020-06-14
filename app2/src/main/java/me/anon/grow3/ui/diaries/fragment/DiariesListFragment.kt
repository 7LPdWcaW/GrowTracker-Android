package me.anon.grow3.ui.diaries.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import me.anon.grow3.databinding.FragmentDiariesBinding
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.diaries.adapter.DiariesListAdapter
import me.anon.grow3.ui.diaries.viewmodel.DiariesListViewModel
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.newTaskForResult
import me.anon.grow3.util.onClick
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class DiariesListFragment : BaseFragment(FragmentDiariesBinding::class.java)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiariesListViewModel.Factory
	private val viewModel: DiariesListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by lazy { binding<FragmentDiariesBinding>() }
	private val adapter by lazy { DiariesListAdapter() }

	override fun bindUi()
	{
		adapter.onItemClick = { item ->
			navigateTo<ViewDiaryFragment> {
				bundleOf(MainActivity.EXTRA_DIARY_ID to item.id)
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
		viewModel.gardens.observe(viewLifecycleOwner) {
			when (it)
			{
				is DataResult.Success -> {
					adapter.items = it.data
					adapter.notifyDataSetChanged()
				}
			}
		}
	}
}
