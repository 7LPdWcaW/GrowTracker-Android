package me.anon.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.observe
import me.anon.lib.ext.viewModelFactory
import me.anon.lib.ext.zipLiveData
import me.anon.view.viewmodel.BootViewModel

/**
 * // TODO: Add class description
 */
class BootActivity2 : BaseActivity()
{
	private val viewModel by viewModels<BootViewModel> { viewModelFactory() }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		viewModel.plantsLoaded.observe(this) { result ->
			if (result.isFailure)
			{
				Toast.makeText(this, "Error loading plants data", Toast.LENGTH_LONG).show()
			}
		}

		viewModel.gardensLoaded.observe(this) { result ->
			if (result.isFailure)
			{
				Toast.makeText(this, "Error loading garden data", Toast.LENGTH_LONG).show()
			}
		}

		arrayListOf(viewModel.plantsLoaded, viewModel.gardensLoaded).zipLiveData().observe(this) { results ->
			val found = results.find { it.isFailure }
			if (found == null)
			{
				val main = Intent(this, MainActivity2::class.java)
				startActivity(main)
				finish()
			}
		}

		viewModel.initialise()
	}
}

