package me.anon.grow3.ui.crud.fragment

import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import me.anon.grow3.data.model.MediumType
import me.anon.grow3.databinding.FragmentCrudDiaryCropBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.Injector
import me.anon.grow3.util.ViewModelProvider
import me.anon.grow3.util.onFocusLoss
import me.anon.grow3.util.toStringOrNull
import javax.inject.Inject

class DiaryCropFragment : BaseFragment(FragmentCrudDiaryCropBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryCropBinding>()

	override fun bindUi()
	{
		viewBindings.cropName.editText!!.onFocusLoss {
			it.text.toStringOrNull()?.let {
//				viewModel.setCrop(
//					name = ValueHolder(it)
//				)
			}
		}
		viewBindings.cropName.editText!!.doAfterTextChanged {
			if (it.isNullOrBlank()) viewBindings.cropName.error = "Required field"
			else viewBindings.cropName.error = null
		}

		viewBindings.cropGenetics.editText!!.onFocusLoss {
//			viewModel.setCrop(
//				genetics = ValueHolder(it.text.toStringOrNull())
//			)
		}

		viewBindings.cropNumPlants.editText!!.onFocusLoss {
//			viewModel.setCrop(
//				numberOfPlants = ValueHolder(it.text.toIntOrNull() ?: 1)
//			)
		}

		viewBindings.mediumTypeOptions.setMenu(MediumType.toMenu())
		viewBindings.mediumTypeOptions.itemSelectListener = { item ->
//			viewModel.setCrop(
//				mediumType = ValueHolder(MediumType.ofId(item.itemId))
//			)
		}

		viewBindings.mediumSize.editText!!.doAfterTextChanged {
			if (!it.isNullOrBlank() && viewBindings.mediumTypeOptions.getSelectedItems().isEmpty()) viewBindings.mediumType.error = "Required field"
			else viewBindings.mediumType.error = null
		}
		viewBindings.mediumSize.editText!!.onFocusLoss {
//			viewModel.setCrop(
//				volume = ValueHolder(it.text.toDoubleOrNull())
//			)
		}
	}

	override fun bindVm()
	{
//		combineTuple(viewModel.diary, viewModel.crop).observe(this) { (diary, crop) ->
//			if (diary?.isSuccess != true || crop == null) return@observe
//			val diary = diary.asSuccess()
//
//			title = string(if (viewModel.newCrop) R.string.add_crop_title else R.string.edit_crop_title)
//			viewBindings.cropName.editText!!.text = crop.name.asEditable()
//			viewBindings.cropGenetics.editText!!.text = crop.genetics?.asEditable()
//			viewBindings.cropNumPlants.editText!!.text = crop.numberOfPlants.toString().asEditable()
//
//			val medium = diary.mediumOf(crop)
//			medium?.let {
//				viewBindings.mediumTypeOptions.checkItems(it.medium.strRes)
//				viewBindings.mediumSize.editText!!.text = it.size.asStringOrNull()?.asEditable()
//			}
//		}
	}
}
