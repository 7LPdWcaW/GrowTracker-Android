package me.anon.grow3.ui.crud.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import me.anon.grow3.data.model.*
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
	private var isNew = false

	override fun bindArguments(bundle: Bundle?)
	{
		super.bindArguments(bundle)
		bundle?.getString(Extras.EXTRA_CROP_ID).let {
			if (it.isNullOrBlank()) crudViewModel.newCrop()
			else crudViewModel.loadCrop(it)
		}
	}

	override fun bindUi()
	{
		viewBindings.removeCrop.onClick {
			requireContext().promptRemove {
				if (it)
				{
					crudViewModel.removeCrop()
					crudViewModel.endCrop()
					(activity as? DiaryActivity)?.popBackStack()
				}
			}
		}

		viewBindings.cropName.editText!!.onFocusLoss {
			it.text.toStringOrNull()?.let {
				crudViewModel.mutateCrop {
					patch(name = ValueHolder(it))
				}
			}
		}
		viewBindings.cropName.editText!!.doAfterTextChanged {
			if (it.isNullOrBlank()) viewBindings.cropName.error = "Required field"
			else viewBindings.cropName.error = null
		}

		viewBindings.cropGenetics.editText!!.onFocusLoss {
			crudViewModel.mutateCrop {
				patch(genetics = ValueHolder(it.text.toStringOrNull()))
			}
		}

		viewBindings.cropNumPlants.editText!!.onFocusLoss {
			crudViewModel.mutateCrop {
				patch(numberOfPlants = ValueHolder(it.text.toIntOrNull() ?: 1))
			}
		}

		viewBindings.mediumCard.isVisible = false
		viewBindings.mediumTypeOptions.singleSelection = true
		viewBindings.mediumTypeOptions.setMenu(MediumType.toMenu())
		viewBindings.mediumTypeOptions.itemSelectListener = { item ->
			crudViewModel.setCropMedium(
				mediumType = ValueHolder(MediumType.ofId(item.itemId))
			)
		}

		viewBindings.mediumSizeUnitOptions.singleSelection = true
		viewBindings.mediumSizeUnitOptions.setMenu(VolumeUnit.toMenu())
		viewBindings.mediumSizeUnitOptions.itemSelectListener = { item ->
			viewBindings.mediumSize.editText!!.text.toDoubleOrNull()?.let { amount ->
				crudViewModel.setCropMedium(
					// only re-save if volume is entered
					volume = ValueHolder(Volume(amount, VolumeUnit.ofId(item.itemId)))
				)
			}
		}

		viewBindings.mediumSize.editText!!.doAfterTextChanged {
			if (!it.isNullOrBlank() && viewBindings.mediumTypeOptions.getSelectedItems().isEmpty()) viewBindings.mediumType.error = "Required field"
			else viewBindings.mediumType.error = null
		}
		viewBindings.mediumSize.editText!!.onFocusLoss {
			crudViewModel.setCropMedium(
				volume = ValueHolder(it.text.toDoubleOrNull()?.let {
					Volume(it, VolumeUnit.ofId(viewBindings.mediumSizeUnitOptions.getSelectedItems().first().itemId))
				})
			)
		}
	}

	init {
		lifecycleScope.launchWhenCreated {
			crudViewModel.state
				.collectLatest { state ->
					val state = state as? DiaryCrudViewModel.UiResult.Loaded
						?: return@collectLatest
					val diary = state.diary
					val crop = state.crop
						?: return@collectLatest

					isNew = crop.isDraft

					viewBindings.cropName.editText!!.text = crop.name.asEditable()
					viewBindings.cropGenetics.editText!!.text = crop.genetics?.asEditable()
					viewBindings.cropNumPlants.editText!!.text = crop.numberOfPlants.toString().asEditable()

					val medium = diary.mediumOf(crop)
					viewBindings.mediumCard.isVisible = medium == null || medium.isDraft

					//					viewBindings.mediumTypeOptions.checkItems(it.medium.strRes)
					//					it.size?.let { size ->
					//						viewBindings.mediumSizeUnitOptions.checkItems(size.unit.strRes)
					//						viewBindings.mediumSize.editText!!.text = size.amount.asStringOrNull()?.asEditable()
					//					}

					viewBindings.includeCardStages.stagesHeader.isVisible = true
					viewBindings.includeCardStages.stagesView.setStages(diary, crop)
					viewBindings.includeCardStages.stagesView.onStageClick = { stage ->
						// diary needs to be saved at this point before modal is opened otherwise changes get overwritten
						requireView().clearFocus()

						(activity as DiaryActivity).openModal(LogActionFragment().apply {
							arguments = bundleOf(
								Extras.EXTRA_DIARY_ID to diary.id,
								Extras.EXTRA_LOG_ID to stage.id,
								Extras.EXTRA_LOG_TYPE to nameOf<StageChange>(),
								Extras.EXTRA_CROP_IDS to arrayOf(crop.id),
								LogActionFragment.EXTRA_SINGLE_CROP to true
							)
						})
					}
					viewBindings.includeCardStages.stagesView.onNewStageClick = {
						// diary needs to be saved at this point before modal is opened otherwise changes get overwritten
						requireView().clearFocus()

						(activity as DiaryActivity).openModal(LogActionFragment().apply {
							arguments = bundleOf(
								Extras.EXTRA_DIARY_ID to diary.id,
								Extras.EXTRA_LOG_TYPE to nameOf<StageChange>(),
								Extras.EXTRA_CROP_IDS to arrayOf(crop.id),
								LogActionFragment.EXTRA_SINGLE_CROP to true
							)
						})
					}
				}
		}
	}

	private fun saveView()
	{
		val type = viewBindings.mediumTypeOptions.getSelectedItems().firstOrNull() ?: return
		val sizeUnit = viewBindings.mediumSizeUnitOptions.getSelectedItems().firstOrNull() ?: return
		val size = viewBindings.mediumSize.editText!!.text.toDoubleOrNull() ?: return

		crudViewModel.setCropMedium(
			mediumType = ValueHolder(MediumType.ofId(type.itemId)),
			volume = ValueHolder(Volume(size, VolumeUnit.ofId(sizeUnit.itemId))),
			draft = false,
		)
	}

	private var backPress = true
	override fun onBackPressed(): Boolean
	{
		if (backPress)
		{
			saveView()
			crudViewModel.saveCropAndFinish()
			backPress = false
			//activity?.onBackPressed()
		}

		return backPress
	}

	override fun onDestroyView()
	{
		//viewModel.crop.value?.let { viewModel.saveCrop(it) }
		super.onDestroyView()
	}
}
