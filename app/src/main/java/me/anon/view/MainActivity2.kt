package me.anon.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import me.anon.grow.R
import me.anon.view.viewmodel.MainViewModel
import me.anon.view.viewmodel.ViewModelFactory

/**
 * // TODO: Add class description
 */
class MainActivity2 : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		var fragment: MainFragment? = supportFragmentManager.findFragmentById(R.id.fragment_holder) as? MainFragment

		if (savedInstanceState == null)
		{
			supportFragmentManager.beginTransaction()
				.add(R.id.fragment_holder, MainFragment())
				.commit()
		}
	}
}

class MainFragment : Fragment()
{
	private val viewModel: MainViewModel by viewModels<MainViewModel> { ViewModelFactory(requireActivity().application as MainApplication2, this) }

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.plant_list_view, container, false)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		setupList()
	}

	private fun setupList()
	{
		viewModel.plants.observe(viewLifecycleOwner) { plants ->
			require(plants != null)
		}
	}
}
