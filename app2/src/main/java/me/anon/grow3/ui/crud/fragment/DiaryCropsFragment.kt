package me.anon.grow3.ui.crud.fragment

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.plusAssign
import androidx.fragment.app.activityViewModels
import me.anon.grow3.R
import androidx.lifecycle.observe
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import kotlinx.android.synthetic.main.fragment_crud_diary_crops.*
import kotlinx.android.synthetic.main.stub_crud_crop.view.*
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import javax.inject.Inject

class DiaryCropsFragment : BaseFragment(R.layout.fragment_crud_diary_crops)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) {
			crops_container.removeAllViews()
			it.crops.forEach { crop ->
				val view = crops_container.inflate<View>(R.layout.stub_crud_crop)
				view.crop_name.text = crop.name
				view.crop_genetics.text = crop.genetics
				view.duplicate.onClick {
					viewModel.addCrop(crop)
				}
				view.onClick {
					// reveal crop edit fragment dialog
				}

				// Broken for now
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
		}
	}
}
