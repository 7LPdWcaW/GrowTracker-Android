package me.anon.grow

import android.os.Bundle
import me.anon.grow.databinding.FragmentHolderBinding
import me.anon.grow.fragment.ActionsListFragment

class ActionsActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.fragment_holder)
		val binding = FragmentHolderBinding.inflate(layoutInflater)
		setSupportActionBar(binding.toolbar)

		if (intent.extras == null || !intent.hasExtra("plant"))
		{
			finish()
			return
		}

		if (supportFragmentManager.findFragmentByTag("fragment") == null)
		{
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_holder, ActionsListFragment.newInstance(intent.extras), "fragment")
				.commit()
		}
	}
}
