package me.anon.grow3.ui.crud.fragment

import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.plusAssign
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import me.anon.grow3.R
import me.anon.grow3.databinding.FragmentCrudDiaryDetailsBinding
import me.anon.grow3.databinding.StubCrudCropBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.common.fragment.DateSelectDialogFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import org.threeten.bp.ZonedDateTime
import java.util.*
import javax.inject.Inject

class DiaryDetailsFragment : BaseFragment(FragmentCrudDiaryDetailsBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryDetailsBinding>()

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			viewBindings.diaryName.editText!!.text = diary.name.asEditable()
			viewBindings.date.editText!!.text = diary.date.asDateTime().asFormattedString().asEditable()
//			viewBindings.includeCardStages.stagesView.setStages(diary)

			viewBindings.cropsContainer.removeAllViews()
			diary.crops.forEach { crop ->
				val view = viewBindings.cropsContainer.inflate<View>(R.layout.stub_crud_crop)
				val cropBindings = StubCrudCropBinding.bind(view)
				cropBindings.cropName.text = crop.name

				cropBindings.cropGenetics.text = crop.genetics
				cropBindings.cropGenetics.isVisible = !crop.genetics.isNullOrBlank()

				cropBindings.duplicate.onClick {
					viewModel.saveCrop(crop.copy(id = UUID.randomUUID().toString()))
				}

				view.onClick {
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

				viewBindings.cropsContainer += view
			}
		}
	}

	override fun bindUi()
	{
		viewBindings.diaryName.editText!!.doAfterTextChanged {
			// don't re-trigger the text change by calling editText.text ...
			val diary = viewModel.diary.value!!
			diary.name = it.toString()
		}

		viewBindings.date.editText!!.onFocus {
			val diary = viewModel.diary.value!!

			it.hideKeyboard()

			val current = diary.date
			DateSelectDialogFragment.show(current, true, childFragmentManager).apply {
				onDateTimeSelected = ::onDateSelected
			}

			viewModel.setDiaryDate(current.asDateTime())
		}

		viewBindings.addCrop.onClick {
			// reveal crop edit fragment dialog
			val navController = findNavController()
			navController.navigate(R.id.page_1_to_2)
		}

//		viewBindings.includeCardStages.stagesHeader.isVisible = true
//		viewBindings.includeCardStages.stagesView.onNewStageClick = {
//			(activity as DiaryActivity).openModal(LogActionFragment().apply {
//				arguments = bundleOf(
//					Extras.EXTRA_LOG_TYPE to nameOf<StageChange>(),
//					Extras.EXTRA_DIARY_ID to viewModel.diary.value!!.id
//				)
//			})
//		}

		attachCallbacks()
	}

	private fun attachCallbacks()
	{
		DateSelectDialogFragment.attach(childFragmentManager, ::onDateSelected, ::onDateDismissed)
	}

	public fun onDateSelected(selectedDate: ZonedDateTime)
	{
		viewModel.setDiaryDate(selectedDate)
	}

	public fun onDateDismissed()
	{
		if (viewBindings.date.editText?.focusSearch(View.FOCUS_RIGHT)?.requestFocus() != true) viewBindings.date.editText?.clearFocus()
	}
}
