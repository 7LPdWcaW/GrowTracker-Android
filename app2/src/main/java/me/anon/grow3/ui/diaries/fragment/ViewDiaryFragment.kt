package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.shortSummary
import me.anon.grow3.databinding.FragmentViewDiaryBinding
import me.anon.grow3.databinding.StubCropBinding
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras.EXTRA_CROP_ID
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.diaries.viewmodel.ViewDiaryViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.DataResult
import javax.inject.Inject

class ViewDiaryFragment : BaseFragment(FragmentViewDiaryBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: ViewDiaryViewModel.Factory
	private val viewModel: ViewDiaryViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentViewDiaryBinding>()

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
				viewBindings.content.updatePadding(left, right = right, bottom = bottom + 72.dp(requireContext()))
			}
		}

		viewBindings.menuFab.setOnClickListener {
			viewBindings.menuFab.isExpanded = !viewBindings.menuFab.isExpanded
			navigationPager?.isUserInputEnabled = !viewBindings.menuFab.isExpanded
		}

		viewBindings.sheet.setOnClickListener {
			viewBindings.menuFab.isExpanded = false
			navigationPager?.isUserInputEnabled = true
		}

		viewBindings.menuAction1.onClick {
			viewBindings.menuFab.isExpanded = false
			navigationPager?.isUserInputEnabled = true
			navigateTo<LogActionBottomSheetFragment>(true)
		}
	}

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			when (diary)
			{
				is DataResult.Error -> {}
				is DataResult.Success -> {
					updateDiaryUi(diary.data)
				}
			}
		}
	}

	private fun updateDiaryUi(diary: Diary)
	{
		viewBindings.collapsingToolbarLayout.title = diary.name
		viewBindings.collapsingToolbarLayout.subtitle = diary.stages().shortSummary()
		viewBindings.stagesView.setStages(diary)

		viewBindings.cropsContainer.removeAllViews()
		diary.crops.mapToView<Crop, StubCropBinding>(container = viewBindings.cropsContainer, mapper = { crop, view ->
			view.cropName.text = crop.name
			view.cropImage.onClick {
				navigateTo<ViewCropFragment>() {
					bundleOf(
						EXTRA_DIARY_ID to diary.id,
						EXTRA_CROP_ID to crop.id
					)
				}
			}
		})
	}
}
