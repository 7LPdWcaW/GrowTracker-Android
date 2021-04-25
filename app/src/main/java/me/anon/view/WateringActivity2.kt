package me.anon.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.fragment_holder.*
import me.anon.grow.R
import me.anon.view.fragment.WateringFragment

/**
 * // TODO: Add class description
 */
class WateringActivity2 : BaseActivity()
{
	companion object
	{
		public const val TAG_FRAGMENT = "fragment"
	}

	class LocalViewModel(
		private val savedStateHandle: SavedStateHandle
	) : ViewModel()
	{
		public var plantId: String? = null
			set(value)
			{
				field = value
				savedStateHandle["plantId"] = plantId
			}
		public var actionId: String? = null
			set(value)
			{
				field = value
				savedStateHandle["actionId"] = actionId
			}

		init {
			plantId = savedStateHandle["plantId"] ?: plantId
			actionId = savedStateHandle["actionId"] ?: actionId
		}
	}

	private val viewModel: LocalViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		setSupportActionBar(toolbar)

		val fragment: WateringFragment? = supportFragmentManager.findFragmentById(R.id.fragment_holder) as? WateringFragment

		supportFragmentManager.beginTransaction()
			//.replace(R.id.fragment_holder, fragment ?: WateringFragment.newInstance(viewModel.plantId!!, viewModel.actionId))
			.commit()
	}
}
