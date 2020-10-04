package me.anon.grow3.ui.crud.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.MediumType
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.databinding.FragmentCrudDiaryCropBinding
import me.anon.grow3.ui.action.fragment.LogActionFragment
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.crud.viewmodel.DiaryCrudViewModel
import me.anon.grow3.util.*
import javax.inject.Inject

class DiaryCropFragment : BaseFragment(FragmentCrudDiaryCropBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var crudViewModelFactory: DiaryCrudViewModel.Factory
	private val crudViewModel: DiaryCrudViewModel by activityViewModels { ViewModelProvider(crudViewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryCropBinding>()

	override fun bindArguments(bundle: Bundle?)
	{
		super.bindArguments(bundle)
		bundle?.getString(Extras.EXTRA_CROP_ID).let {
			if (it.isNullOrBlank()) crudViewModel.cropVm.new()
			else crudViewModel.cropVm.load(it)
		}
	}

	override fun bindUi()
	{
		viewBindings.cropName.editText!!.onFocusLoss {
			it.text.toStringOrNull()?.let {
				crudViewModel.setCrop(
					name = ValueHolder(it)
				)
			}
		}
		viewBindings.cropName.editText!!.doAfterTextChanged {
			if (it.isNullOrBlank()) viewBindings.cropName.error = "Required field"
			else viewBindings.cropName.error = null
		}

		viewBindings.cropGenetics.editText!!.onFocusLoss {
			crudViewModel.setCrop(
				genetics = ValueHolder(it.text.toStringOrNull())
			)
		}

		viewBindings.cropNumPlants.editText!!.onFocusLoss {
			crudViewModel.setCrop(
				numberOfPlants = ValueHolder(it.text.toIntOrNull() ?: 1)
			)
		}

		viewBindings.mediumTypeOptions.setMenu(MediumType.toMenu())
		viewBindings.mediumTypeOptions.itemSelectListener = { item ->
			crudViewModel.setCrop(
				mediumType = ValueHolder(MediumType.ofId(item.itemId))
			)
		}

		viewBindings.mediumSize.editText!!.doAfterTextChanged {
			if (!it.isNullOrBlank() && viewBindings.mediumTypeOptions.getSelectedItems().isEmpty()) viewBindings.mediumType.error = "Required field"
			else viewBindings.mediumType.error = null
		}
		viewBindings.mediumSize.editText!!.onFocusLoss {
			crudViewModel.setCrop(
				volume = ValueHolder(it.text.toDoubleOrNull())
			)
		}
	}

	override fun bindVm()
	{
		crudViewModel.cropVm.crop.observe(viewLifecycleOwner) { crop: Crop ->
			val diary = crudViewModel.diaryVm.diary.value
			diary ?: return@observe

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
				// save the diary so it includes the new crop?
				//viewModel.saveCrop(crop)
				(activity as DiaryActivity).openModal(LogActionFragment().apply {
					arguments = bundleOf(
						Extras.EXTRA_DIARY_ID to diary.id,
						Extras.EXTRA_LOG_TYPE to nameOf<StageChange>(),
						Extras.EXTRA_CROP_IDS to arrayOf(crop.id)
					)
				})
			}
		}
	}

	private var backPress = true
	override fun onBackPressed(): Boolean
	{
		if (backPress && crudViewModel.cropVm.isNew)
		{
			activity?.promptExit {
				backPress = false
				crudViewModel.cropVm.remove()
				activity?.onBackPressed()
			}
		}
		else
		{
			backPress = false
		}

		return backPress
	}

	override fun onDestroyView()
	{
		//viewModel.crop.value?.let { viewModel.saveCrop(it) }
		super.onDestroyView()
	}
}
