package me.anon.grow3.ui.diaries.fragment

import androidx.core.os.bundleOf
import androidx.core.view.size
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.freelapp.flowlifecycleobserver.collectWhileStarted
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.LogConstants
import me.anon.grow3.databinding.FragmentViewDiaryBinding
import me.anon.grow3.databinding.StubMenuLogActionBinding
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.view.StagesCard
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.diaries.view.DiaryCropsCard
import me.anon.grow3.ui.diaries.view.DiaryLinksCard
import me.anon.grow3.ui.diaries.viewmodel.ViewDiaryViewModel
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.*
import me.anon.grow3.view.adapter.CardListAdapter
import javax.inject.Inject

class ViewDiaryFragment : BaseFragment(FragmentViewDiaryBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: ViewDiaryViewModel.Factory
	private val viewModel: ViewDiaryViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentViewDiaryBinding>()
	@Inject internal lateinit var viewAdapter: CardListAdapter

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

		viewBindings.menuLogContainer.removeViewsBefore(count = 1)
		LogConstants.quickMenu.sortedBy { it.name }.forEach { type ->
			val menuView = StubMenuLogActionBinding.inflate(layoutInflater, viewBindings.menuLogContainer, false)
			menuView.logName.text = type.name
			viewBindings.menuLogContainer.addView(menuView.root, viewBindings.menuLogContainer.size - 1)

			// routing
			menuView.root.onClick {
				viewBindings.menuFab.isExpanded = false
				navigationPager?.isUserInputEnabled = true
				navigateTo<LogActionBottomSheetFragment>(true) {
					bundleOf(
						Extras.EXTRA_DIARY_ID to viewModel.diaryId,
						Extras.EXTRA_LOG_TYPE to type.type.name
					)
				}
			}
		}
	}

	override fun bindVm()
	{
		viewModel.state
			.collectWhileStarted(this) { state ->
				when (state)
				{
					is ViewDiaryViewModel.UiResult.Loaded -> updateDiaryUi(state.diary)
					is ViewDiaryViewModel.UiResult.Removed -> {
						navigateTo<EmptyFragment>()
						(activity as? MainActivity)?.openMenu()
					}
					else -> {}
				}
			}
	}

	private fun updateDiaryUi(diary: Diary)
	{
		viewBindings.toolbar.title = diary.name
		viewBindings.toolbar.navigationIcon = R.drawable.ic_baseline_menu_24.drawable(requireContext())
		viewBindings.toolbar.setNavigationOnClickListener {
			(requireActivity() as? MainActivity)?.openMenu()
		}

		//viewBindings.collapsingToolbarLayout.title = diary.name
//		viewBindings.collapsingToolbarLayout.subtitle = diary.stages().shortSummary()

		viewBindings.toolbar.menu.clear()
		viewBindings.toolbar.inflateMenu(R.menu.menu_diary)
		viewBindings.toolbar.setOnMenuItemClickListener { item ->
			when (item.itemId)
			{
				R.id.menu_edit -> newTask<DiaryActivity> {
					putExtra(Extras.EXTRA_DIARY_ID, diary.id)
				}
			}

			true
		}

		viewAdapter.newStack {
			add(StagesCard(diary = diary, title = "Stages summary"))

			if (diary.crops.isNotEmpty())
			{
				add(DiaryCropsCard(diary = diary, title = "Crops"))
			}

			add(DiaryLinksCard(diary = diary))
		}
	}
}
