package me.anon.grow3.ui.crops.fragment

import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import me.anon.grow3.data.model.Crop
import me.anon.grow3.databinding.FragmentViewCropBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.view.LogMediumCard
import me.anon.grow3.ui.common.view.StagesCard
import me.anon.grow3.ui.crops.view.CropDetailsCard
import me.anon.grow3.ui.crops.view.CropLinksCard
import me.anon.grow3.ui.crops.viewmodel.ViewCropViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.dp
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.updateMargin
import me.anon.grow3.view.adapter.CardListAdapter
import javax.inject.Inject

class ViewCropFragment : BaseFragment(FragmentViewCropBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: ViewCropViewModel.Factory
	private val viewModel: ViewCropViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentViewCropBinding>()
	private val viewAdapter = CardListAdapter()

	override fun bindUi()
	{
		insets.observe(viewLifecycleOwner) {
			viewBindings.includeToolbar.toolbar.updateMargin(it.left, it.top, it.right)
			viewBindings.recyclerView.updatePadding(it.left, right = it.right, bottom = it.bottom + 72.dp(requireContext()))
		}

		viewBindings.recyclerView.adapter = viewAdapter
		viewBindings.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
	}

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
		viewModel.diary.value?.asSuccess()?.let { diary ->
			viewAdapter.cards.clear()
			viewAdapter.cards.add(StagesCard(diary = diary, crop = crop))
			viewAdapter.cards.add(CropDetailsCard(diary = diary, crop = crop))

			diary.mediumOf(crop)?.let { medium ->
				viewAdapter.cards.add(LogMediumCard(diary = diary, crop = crop, medium = medium))
			}

			viewAdapter.cards.add(CropLinksCard(diary = diary, crop = crop))

			viewAdapter.notifyDataSetChanged()
		}
	}
}
