package me.anon.grow3.ui.crud.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.zhuinden.livedatacombinetuplekt.combineTuple
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.MediumType
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.databinding.FragmentCrudDiaryCropBinding
import me.anon.grow3.ui.action.fragment.LogActionFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import javax.inject.Inject

class DiaryCropFragment : BaseFragment(FragmentCrudDiaryCropBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryCropBinding>()

	override fun bindArguments(bundle: Bundle?)
	{
		super.bindArguments(bundle)
		bundle?.getString(Extras.EXTRA_CROP_ID).let {
			it.isNullOrBlank() then viewModel.newCrop() ?: viewModel.editCrop(it!!)
		}
	}

	override fun bindUi()
	{
		viewBindings.cropName.editText!!.onFocusLoss {
			it.text.toStringOrNull()?.let {
				viewModel.setCrop(
					name = ValueHolder(it)
				)
			}
		}
		viewBindings.cropName.editText!!.doAfterTextChanged {
			if (it.isNullOrBlank()) viewBindings.cropName.error = "Required field"
			else viewBindings.cropName.error = null
		}

		viewBindings.cropGenetics.editText!!.onFocusLoss {
			viewModel.setCrop(
				genetics = ValueHolder(it.text.toStringOrNull())
			)
		}

		viewBindings.cropNumPlants.editText!!.onFocusLoss {
			viewModel.setCrop(
				numberOfPlants = ValueHolder(it.text.toIntOrNull() ?: 1)
			)
		}

		viewBindings.mediumTypeOptions.setMenu(MediumType.toMenu())
		viewBindings.mediumTypeOptions.itemSelectListener = { item ->
			viewModel.setCrop(
				mediumType = ValueHolder(MediumType.ofId(item.itemId))
			)
		}

		viewBindings.mediumSize.editText!!.doAfterTextChanged {
			if (!it.isNullOrBlank() && viewBindings.mediumTypeOptions.getSelectedItems().isEmpty()) viewBindings.mediumType.error = "Required field"
			else viewBindings.mediumType.error = null
		}
		viewBindings.mediumSize.editText!!.onFocusLoss {
			viewModel.setCrop(
				volume = ValueHolder(it.text.toDoubleOrNull())
			)
		}
	}

	override fun bindVm()
	{
		combineTuple(viewModel.diary, viewModel.crop).observe(viewLifecycleOwner) { (diary: Diary?, crop: Crop?) ->
			if (diary == null || crop == null) return@observe

			viewBindings.cropName.editText!!.text = crop.name.asEditable()
			viewBindings.cropGenetics.editText!!.text = crop.genetics?.asEditable()
			viewBindings.cropNumPlants.editText!!.text = crop.numberOfPlants.toString().asEditable()

			val medium = diary.mediumOf(crop)
			medium?.let {
				viewBindings.mediumTypeOptions.checkItems(it.medium.strRes)
				viewBindings.mediumSize.editText!!.text = it.size.asStringOrNull()?.asEditable()
			}

			viewBindings.includeCardStages.stagesHeader.isVisible = true
			viewBindings.includeCardStages.stagesView.setStages(diary, crop)
			viewBindings.includeCardStages.stagesView.onNewStageClick = {
				(activity as DiaryActivity).openModal(LogActionFragment().apply {
					arguments = bundleOf(
						Extras.EXTRA_DIARY_ID to diary.id,
						Extras.EXTRA_LOG_TYPE to nameOf<StageChange>()
					)
				})
			}
		}
	}

	private var backPress = true
	override fun onBackPressed(): Boolean
	{
		if (backPress)
		{
			activity?.promptExit {
				backPress = false
				viewModel.removeCrop()
				activity?.onBackPressed()
			}
		}

		return backPress
	}

	override fun onDestroyView()
	{
		viewModel.crop.value?.let { viewModel.saveCrop(it) }
		super.onDestroyView()
	}
}
