package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Water
import me.anon.grow3.data.model.shortSummary
import me.anon.grow3.databinding.FragmentViewDiaryBinding
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.view.StagesCard
import me.anon.grow3.ui.diaries.view.DiaryCropsCard
import me.anon.grow3.ui.diaries.view.DiaryLinksCard
import me.anon.grow3.ui.diaries.viewmodel.ViewDiaryViewModel
import me.anon.grow3.util.*
import me.anon.grow3.view.adapter.CardListAdapter
import javax.inject.Inject

class ViewDiaryFragment : BaseFragment(FragmentViewDiaryBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: ViewDiaryViewModel.Factory
	private val viewModel: ViewDiaryViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentViewDiaryBinding>()
	private val viewAdapter = CardListAdapter()

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		setToolbar(viewBindings.toolbar)
	}

	override fun onBackPressed(): Boolean
		= viewBindings.menuFab.isExpanded.also { viewBindings.menuFab.isExpanded = false }

	override fun bindUi()
	{
		insets.observe(viewLifecycleOwner) {
			with (it) {
				viewBindings.menuFab.updateMargin(bottom = bottom + 16.dp(requireContext()), top = top + 16.dp(requireContext()))
				viewBindings.sheet.updatePadding(left, top, right, bottom)
				viewBindings.toolbar.updateMargin(left, top, right)
				viewBindings.recyclerView.updatePadding(left, right = right, bottom = bottom + 72.dp(requireContext()))
			}
		}

		viewBindings.recyclerView.adapter = viewAdapter
		viewBindings.recyclerView.layoutManager = LinearLayoutManager(requireContext())

		viewBindings.menuFab.setOnClickListener {
			viewBindings.menuFab.isExpanded = !viewBindings.menuFab.isExpanded
			navigationPager?.isUserInputEnabled = !viewBindings.menuFab.isExpanded
		}

		viewBindings.sheet.setOnClickListener {
			viewBindings.menuFab.isExpanded = false
			navigationPager?.isUserInputEnabled = true
		}

		viewBindings.menuLogWater.onClick {
			viewBindings.menuFab.isExpanded = false
			navigationPager?.isUserInputEnabled = true
			navigateTo<LogActionBottomSheetFragment>(true) {
				bundleOf(
					Extras.EXTRA_DIARY_ID to viewModel.diaryId,
					Extras.EXTRA_LOG_TYPE to nameOf<Water>()
				)
			}
		}
	}

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { state ->
			when (state)
			{
				is ViewDiaryViewModel.UiResult.Loaded -> {
					updateDiaryUi(state.diary)
				}
				else -> {}
			}
		}
	}

	private fun updateDiaryUi(diary: Diary)
	{
		viewBindings.collapsingToolbarLayout.title = diary.name
		viewBindings.collapsingToolbarLayout.subtitle = diary.stages().shortSummary()

		viewAdapter.newStack {
			add(StagesCard(diary = diary, title = "Stages summary"))
			add(DiaryCropsCard(diary = diary, title = "Crops"))
			add(DiaryLinksCard(diary = diary))
		}
	}
}
