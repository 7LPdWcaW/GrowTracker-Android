package me.anon.grow3.ui.crud.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_crud_crop.*
import me.anon.grow3.R
import me.anon.grow3.data.model.MediumType
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.crud.viewmodel.CropViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.isSuccess
import javax.inject.Inject

class CropActivity : BaseActivity(R.layout.activity_crud_crop)
{
	companion object
	{
		public const val EXTRA_DIARY_ID = "diary.id"
		public const val EXTRA_CROP_ID = "crop.id"
	}

	@Inject internal lateinit var viewModelFactory: CropViewModel.Factory
	private val viewModel: CropViewModel by viewModels { ViewModelProvider(viewModelFactory, this) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		component.inject(this)

		toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
			navigationIcon = R.drawable.ic_check.drawable(context, R.attr.textOnSurface.resColor(context))
		}
		statusBarColor = 0xffffffff.toInt()

		bindUi()
		bindVm()

		viewModel.init(intent.extras?.getString(EXTRA_DIARY_ID)!!, intent.extras?.getString(EXTRA_CROP_ID))
	}

	private fun bindUi()
	{
		crop_name.editText!!.onFocusLoss {
			it.text.toStringOrNull()?.let {
				viewModel.setCrop(
					name = ValueHolder(it)
				)
			}
		}
		crop_name.editText!!.doAfterTextChanged {
			if (it.isNullOrBlank()) crop_name.error = "Required field"
			else crop_name.error = null
		}

		crop_genetics.editText!!.onFocusLoss {
			viewModel.setCrop(
				genetics = ValueHolder(it.text.toStringOrNull())
			)
		}

		crop_num_plants.editText!!.onFocusLoss {
			viewModel.setCrop(
				numberOfPlants = ValueHolder(it.text.toIntOrNull() ?: 1)
			)
		}

		medium_type_options.setMenu(MediumType.toMenu())
		medium_type_options.itemSelectListener = { item ->
			viewModel.setCrop(
				mediumType = ValueHolder(MediumType.ofId(item.itemId))
			)
		}

		medium_size.editText!!.doAfterTextChanged {
			if (!it.isNullOrBlank() && medium_type_options.getSelectedItems().isEmpty()) medium_type.error = "Required field"
			else medium_type.error = null
		}
		medium_size.editText!!.onFocusLoss {
			viewModel.setCrop(
				volume = ValueHolder(it.text.toDoubleOrNull())
			)
		}
	}

	private fun bindVm()
	{
		viewModel.diary.combine(viewModel.crop) { diary, crop ->
			Pair(diary, crop)
		}.observe(this) {
			if (!it.first.isSuccess) return@observe
			val diary = it.first.asSuccess()
			val crop = it.second

			title = string(if (viewModel.newCrop) R.string.add_crop_title else R.string.edit_crop_title)
			crop_name.editText!!.text = crop.name.asEditable()
			crop_genetics.editText!!.text = crop.genetics?.asEditable()
			crop_num_plants.editText!!.text = crop.numberOfPlants.toString().asEditable()

			val medium = diary.mediumOf(crop)
			medium?.let {
				medium_type_options.checkItems(it.medium.strRes)
				medium_size.editText!!.text = it.size.asStringOrNull()?.asEditable()
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
