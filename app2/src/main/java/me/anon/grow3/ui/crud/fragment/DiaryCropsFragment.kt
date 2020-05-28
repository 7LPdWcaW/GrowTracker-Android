package me.anon.grow3.ui.crud.fragment

import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.plusAssign
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_crud_diary_crops.*
import kotlinx.android.synthetic.main.stub_crud_crop.view.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.activity.CropActivity
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class DiaryCropsFragment : BaseFragment(R.layout.fragment_crud_diary_crops)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			if (!diary.isSuccess) return@observe

			val diary = diary.asSuccess()
			crops_container.removeAllViews()
			diary.crops.forEach { crop ->
				val view = crops_container.inflate<View>(R.layout.stub_crud_crop)
				view.crop_name.text = crop.name

				view.crop_genetics.text = crop.genetics
				view.crop_genetics.isVisible = crop.genetics.isNotBlank()

				view.duplicate.onClick {
					viewModel.addCrop(crop.copy(id = UUID.randomUUID().toString()))
				}

				view.onClick {
					// reveal crop edit fragment dialog
					navigateForResult<CropActivity> {
						putExtra(CropActivity.EXTRA_DIARY_ID, diary.id)
						putExtra(CropActivity.EXTRA_CROP_ID, crop.id)
					}
				}

//				 Broken for now
//				BadgeUtils.attachBadgeDrawable(BadgeDrawable.create(view.context).apply {
//					this.number = crop.numberOfPlants
//					this.backgroundColor = R.attr.colorSecondary.resColor(view.context)
//					this.badgeGravity = BadgeDrawable.TOP_END
//				}, view.crop_image, null))

				crops_container += view
			}
		}
	}

	override fun bindUi()
	{
		add_crop.onClick {
			// reveal crop edit fragment dialog
			navigateForResult<CropActivity> {
				putExtra(CropActivity.EXTRA_DIARY_ID, viewModel.diary.value?.asSuccess()?.id)
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		when (requestCode)
		{
			code<CropActivity>() -> {
				viewModel.refresh()
				Timber.e("refresh $requestCode $resultCode")
			}
		}

		super.onActivityResult(requestCode, resultCode, data)
	}
}
