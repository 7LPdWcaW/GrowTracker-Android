package me.anon.grow3.ui.crud.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.observe
import com.zhuinden.livedatacombinetuplekt.combineTuple
import me.anon.grow3.R
import me.anon.grow3.data.model.MediumType
import me.anon.grow3.databinding.ActivityCrudCropBinding
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.crud.viewmodel.CropViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.isSuccess
import javax.inject.Inject

class CropActivity : BaseActivity(ActivityCrudCropBinding::class)
{
	override val injector: Injector = { it.inject(this) }
	@Inject internal lateinit var viewModelFactory: CropViewModel.Factory
	private val viewModel: CropViewModel by viewModels { ViewModelProvider(viewModelFactory, this, intent.extras) }
	private val viewBindings by viewBinding<ActivityCrudCropBinding>()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		toolbar = viewBindings.toolbar.apply {
			navigationIcon = R.drawable.ic_check.drawable(context, R.attr.textOnSurface.resColor(context))
		}
		statusBarColor = 0xffffffff.toInt()
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
		combineTuple(viewModel.diary, viewModel.crop).observe(this) { (diary, crop) ->
			if (diary?.isSuccess != true || crop == null) return@observe
			val diary = diary.asSuccess()

			title = string(if (viewModel.newCrop) R.string.add_crop_title else R.string.edit_crop_title)
			viewBindings.cropName.editText!!.text = crop.name.asEditable()
			viewBindings.cropGenetics.editText!!.text = crop.genetics?.asEditable()
			viewBindings.cropNumPlants.editText!!.text = crop.numberOfPlants.toString().asEditable()

			val medium = diary.mediumOf(crop)
			medium?.let {
				viewBindings.mediumTypeOptions.checkItems(it.medium.strRes)
				viewBindings.mediumSize.editText!!.text = it.size.asStringOrNull()?.asEditable()
			}
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		if (item.itemId == android.R.id.home)
		{
			clearFocus()
			onBackPressed()
		}

		return super.onOptionsItemSelected(item)
	}

	override fun onBackPressed()
	{
		clearFocus()
		viewModel.reset()
		super.onBackPressed()
	}
}
