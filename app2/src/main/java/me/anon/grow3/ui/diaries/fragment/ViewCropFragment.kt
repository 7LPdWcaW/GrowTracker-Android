package me.anon.grow3.ui.diaries.fragment

import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow3.data.model.Crop
import me.anon.grow3.databinding.FragmentViewCropBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.diaries.viewmodel.ViewCropViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
import javax.inject.Inject

class ViewCropFragment : BaseFragment(FragmentViewCropBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: ViewCropViewModel.Factory
	private val viewModel: ViewCropViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentViewCropBinding>()

	override fun bindUi()
	{
		with (insets) {
			viewBindings.includeToolbar.toolbar.updateMargin(left, top, right)
			viewBindings.content.updatePadding(left, right = right, bottom = bottom + 72.dp(requireContext()))
		}
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
			viewBindings.stagesView.setStages(diary, crop)
			viewBindings.stagesView.isNestedScrollingEnabled = true
		}

		viewBindings.cropName.editText!!.text = crop.name.asEditable()
		viewBindings.cropGenetics.editText!!.text = crop.genetics?.asEditable()
		viewBindings.cropNumPlants.editText!!.text = crop.numberOfPlants.asString().asEditable()
	}
}
