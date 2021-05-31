package me.anon.grow3.ui.crud.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.freelapp.flowlifecycleobserver.collectWhileStarted
import me.anon.grow3.R
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
		viewBindings.done.onClick {
			crudViewModel.mutateCrop { this }
			crudViewModel.endCrop()
			(activity as? DiaryActivity)?.popBackStack()
		}

		viewBindings.removeCrop.onClick {
			requireContext().promptRemove {
				crudViewModel.removeCrop()
				crudViewModel.endCrop()
				(activity as? DiaryActivity)?.popBackStack()
			}
		}

		viewBindings.cropName.editText!!.onFocusLoss {
			it.text.toStringOrNull()?.let {
				crudViewModel.mutateCrop {
					applyValues(name = ValueHolder(it))
				}
			}
		}
		viewBindings.cropName.editText!!.doAfterTextChanged {
			if (it.isNullOrBlank()) viewBindings.cropName.error = "Required field"
			else viewBindings.cropName.error = null
		}

		viewBindings.cropGenetics.editText!!.onFocusLoss {
			crudViewModel.mutateCrop {
				applyValues(genetics = ValueHolder(it.text.toStringOrNull()))
			}
		}

		viewBindings.cropNumPlants.editText!!.onFocusLoss {
			crudViewModel.mutateCrop {
				applyValues(numberOfPlants = ValueHolder(it.text.toIntOrNull() ?: 1))
			}
		}

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

	override fun bindVm()
	{
		crudViewModel.state
			.collectWhileStarted(this) { state ->
				val state = state as? DiaryCrudViewModel.UiResult.Loaded ?: return@collectWhileStarted
				val diary = state.diary
				val crop = state.crop ?: return@collectWhileStarted
				isNew = crop.isDraft

				viewBindings.contentContainer.updatePadding(
					bottom = (if (isNew) R.dimen.fab_spacing else R.dimen.content_margin).dimen(requireContext()).toInt()
				)

				viewBindings.done.isVisible = isNew
				viewBindings.removeCrop.isVisible = !isNew

				viewBindings.cropName.editText!!.text = crop.name.asEditable()
				viewBindings.cropGenetics.editText!!.text = crop.genetics?.asEditable()
				viewBindings.cropNumPlants.editText!!.text = crop.numberOfPlants.toString().asEditable()

				val medium = diary.mediumOf(crop)
				medium?.let {
					viewBindings.mediumTypeOptions.checkItems(it.medium.strRes)
					it.size?.let { size ->
						viewBindings.mediumSizeUnitOptions.checkItems(size.unit.strRes)
						viewBindings.mediumSize.editText!!.text = size.amount.asStringOrNull()?.asEditable()
					}
				}

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

	private var backPress = true
	override fun onBackPressed(): Boolean
	{
		if (backPress && isNew)
		{
			activity?.promptExit {
				backPress = false
				crudViewModel.removeCrop()
				crudViewModel.endCrop()
				activity?.onBackPressed()
			}
		}
		else
		{
			requireView().clearFocus()
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
