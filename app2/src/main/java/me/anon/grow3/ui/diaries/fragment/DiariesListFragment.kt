package me.anon.grow3.ui.diaries.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_diaries.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.MainActivity
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.diaries.adapter.DiariesListAdapter
import me.anon.grow3.ui.diaries.viewmodel.DiariesListViewModel
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.navigateForResult
import me.anon.grow3.util.onClick
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class DiariesListFragment : BaseFragment(R.layout.fragment_diaries)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiariesListViewModel.Factory
	private val viewModel: DiariesListViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val adapter by lazy { DiariesListAdapter() }

	override fun bindUi()
	{
		adapter.onItemClick = { item ->
			(activity as MainActivity).openDiary(item.id)
		}

		new_diary.onClick {
			navigateForResult<DiaryActivity>()
		}

		recycler_view.adapter = adapter
		recycler_view.layoutManager = LinearLayoutManager(requireContext())
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
