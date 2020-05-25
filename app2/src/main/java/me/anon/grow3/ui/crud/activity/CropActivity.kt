package me.anon.grow3.ui.crud.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
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

		setSupportActionBar(findViewById(R.id.toolbar))
		title = "Edit Crop"
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_check.drawable(this, R.attr.textOnSurface.resColor(this)))

		bindUi()
		bindVm()

		viewModel.init(intent.extras?.getString(EXTRA_DIARY_ID)!!, intent.extras?.getString(EXTRA_CROP_ID))
	}

	private fun bindUi()
	{

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
			// save and quit
		}

		return super.onOptionsItemSelected(item)
	}

	override fun onBackPressed()
	{
		super.onBackPressed()
		// show exit dialog
	}
}
