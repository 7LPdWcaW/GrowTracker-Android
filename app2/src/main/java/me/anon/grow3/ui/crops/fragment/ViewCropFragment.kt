package me.anon.grow3.ui.crops.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.data.model.Crop
import me.anon.grow3.ui.base.CardListFragment
import me.anon.grow3.ui.common.view.LogMediumCard
import me.anon.grow3.ui.common.view.StagesCard
import me.anon.grow3.ui.crops.view.CropDetailsCard
import me.anon.grow3.ui.crops.view.CropLinksCard
import me.anon.grow3.ui.crops.viewmodel.ViewCropViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.view.adapter.plusAssign
import javax.inject.Inject

class ViewCropFragment : CardListFragment()
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: ViewCropViewModel.Factory
	private val viewModel: ViewCropViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.crop.observe(viewLifecycleOwner) {
			when (it)
			{
				is DataResult.Success -> updateCropUi(it.data)
				else -> throw IllegalAccessError("Could not load crop")
			}
		}
	}

	private fun updateCropUi(crop: Crop)
	{
		requireActivity().title = crop.name

		viewModel.diary.value?.asSuccess()?.let { diary ->
			viewAdapter += StagesCard(diary = diary, crop = crop)
			viewAdapter += CropDetailsCard(diary = diary, crop = crop)

			diary.mediumOf(crop)?.let { medium ->
				viewAdapter += LogMediumCard(title = "Medium details", diary = diary, crop = crop, medium = medium)
			}

			viewAdapter += CropLinksCard(diary = diary, crop = crop)
			viewAdapter.notifyDataSetChanged()
		}
	}
}
