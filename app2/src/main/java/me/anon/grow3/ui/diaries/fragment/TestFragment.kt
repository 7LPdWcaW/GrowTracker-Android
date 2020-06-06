package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import me.anon.grow3.R
import kotlin.random.Random

class TestFragment : Fragment(R.layout.fragment_test2)
{
	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		requireView().setBackgroundColor(Random.nextLong().and(0xffffff).or(0xff000000).toInt())
	}
}
