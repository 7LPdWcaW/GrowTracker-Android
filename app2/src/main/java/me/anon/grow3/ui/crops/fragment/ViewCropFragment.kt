package me.anon.grow3.ui.crops.fragment

import androidx.fragment.app.viewModels
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.common.view.LogMediumCard
import me.anon.grow3.ui.common.view.StagesCard
import me.anon.grow3.ui.crops.view.CropDetailsCard
import me.anon.grow3.ui.crops.view.CropLinksCard
import me.anon.grow3.ui.crops.viewmodel.ViewCropViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import javax.inject.Inject

class ViewCropFragment : CardListFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: ViewCropViewModel.Factory
	private val viewModel: ViewCropViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.state.observe(viewLifecycleOwner) { state ->
			val state = state as? ViewCropViewModel.UiResult.Loaded ?: return@observe
			updateCropUi(state.diary, state.crop)
		}
	}

	private fun updateCropUi(diary: Diary, crop: Crop)
	{
		requireActivity().title = crop.name

		viewAdapter.newStack {
			add(StagesCard(diary = diary, crop = crop))
			add(CropDetailsCard(diary = diary, crop = crop))
			diary.mediumOf(crop)?.let { medium ->
				add(LogMediumCard(title = "Medium details", diary = diary, crop = crop, medium = medium))
			}
			add(CropLinksCard(diary = diary, crop = crop))
		}
	}
}
