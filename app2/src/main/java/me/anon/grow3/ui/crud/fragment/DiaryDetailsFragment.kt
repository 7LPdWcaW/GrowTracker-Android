package me.anon.grow3.ui.crud.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import me.anon.grow3.R
import me.anon.grow3.data.model.Crop
import me.anon.grow3.databinding.FragmentCrudDiaryDetailsBinding
import me.anon.grow3.databinding.StubCrudCropBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryCrudViewModel
import me.anon.grow3.util.*
import org.threeten.bp.ZonedDateTime
import java.util.*
import javax.inject.Inject

class DiaryDetailsFragment : BaseFragment(FragmentCrudDiaryDetailsBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var crudViewModelFactory: DiaryCrudViewModel.Factory
	private val crudViewModel: DiaryCrudViewModel by activityViewModels { ViewModelProvider(crudViewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryDetailsBinding>()

	override fun bindArguments(bundle: Bundle?)
	{
		super.bindArguments(bundle)
	}

	override fun bindVm()
	{
		crudViewModel.diaryVm.diary.observe(viewLifecycleOwner) { diary ->
			viewBindings.diaryName.editText!!.text = diary.name.asEditable()
			viewBindings.date.editText!!.text = diary.date.asDateTime().asFormattedString().asEditable()

			viewBindings.cropsContainer.removeAllViews()
			diary.crops.mapToView<Crop, StubCrudCropBinding>(viewBindings.cropsContainer) { crop, cropBindings ->
				cropBindings.cropName.text = crop.name

				cropBindings.cropGenetics.text = crop.genetics
				cropBindings.cropGenetics.isVisible = !crop.genetics.isNullOrBlank()

				cropBindings.duplicate.onClick {
					crudViewModel.cropVm.save(crop.copy(id = UUID.randomUUID().toString()))
				}

				cropBindings.root.onClick {
					// reveal crop edit fragment dialog
					val navController = findNavController()
					navController.navigate(R.id.page_1_to_2, bundleOf(Extras.EXTRA_CROP_ID to crop.id))
				}

//				 Broken for now
//				BadgeUtils.attachBadgeDrawable(BadgeDrawable.create(view.context).apply {
//					this.number = crop.numberOfPlants
//					this.backgroundColor = R.attr.colorSecondary.resColor(view.context)
//					this.badgeGravity = BadgeDrawable.TOP_END
//				}, view.crop_image, null))
			}
		}
	}

	override fun bindUi()
	{
//		viewBindings.diaryName.editText!!.doAfterTextChanged {
//			// don't re-trigger the text change by calling editText.text ...
//			val diary = crudViewModel.diary.value!!
//			diary.name = it.toString()
//		}
//
//		viewBindings.date.editText!!.onFocus {
//			val diary = crudViewModel.diary.value!!
//
//			it.hideKeyboard()
//
//			val current = diary.date
//			DateSelectDialogFragment.show(current, true, childFragmentManager).apply {
//				onDateTimeSelected = ::onDateSelected
//				onDismiss = ::onDateDismissed
//			}
//
//			crudViewModel.setDiaryDate(current.asDateTime())
//		}
//
//		viewBindings.addCrop.onClick {
//			// reveal crop edit fragment dialog
//			val navController = findNavController()
//			navController.navigate(R.id.page_1_to_2)
//		}
//
//		attachCallbacks()
	}

	private fun attachCallbacks()
	{
		DateSelectDialogFragment.attach(childFragmentManager, ::onDateSelected, ::onDateDismissed)
	}

	public fun onDateSelected(selectedDate: ZonedDateTime)
	{
		//crudViewModel.setDiaryDate(selectedDate)
	}

	public fun onDateDismissed()
	{
		if (viewBindings.date.editText?.focusSearch(View.FOCUS_RIGHT)?.requestFocus() != true) viewBindings.date.editText?.clearFocus()
	}
}
