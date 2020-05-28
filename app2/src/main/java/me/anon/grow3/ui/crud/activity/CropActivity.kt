package me.anon.grow3.ui.crud.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_crud_crop.*
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.crud.viewmodel.CropViewModel
import me.anon.grow3.util.*
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
		title = "Edit Crop"
		statusBarColor = 0xffffffff.toInt()

		bindUi()
		bindVm()

		viewModel.init(intent.extras?.getString(EXTRA_DIARY_ID)!!, intent.extras?.getString(EXTRA_CROP_ID))
	}

	private fun bindUi()
	{
		crop_name.editText!!.doAfterTextChanged {
			// don't re-trigger the text change by calling editText.text ...
			viewModel.crop.value?.name = it.toString()
		}

		crop_genetics.editText!!.doAfterTextChanged {
			// don't re-trigger the text change by calling editText.text ...
			viewModel.crop.value?.genetics = it.toString()
		}
	}

	private fun bindVm()
	{
		viewModel.crop.observe(this) {
			crop_name.editText!!.text = it.name.asEditable()
			crop_genetics.editText!!.text = it.genetics.asEditable()
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		if (item.itemId == android.R.id.home)
		{
//			viewModel.save()
			onBackPressed()
		}

		return super.onOptionsItemSelected(item)
	}

	override fun onBackPressed()
	{
		// perhaps all changes should always save?
//		promptExit {
			viewModel.reset()
			super.onBackPressed()
//		}
	}
}
