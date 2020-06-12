package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import me.anon.grow3.R
import me.anon.grow3.util.applyWindowInsets

class LogListFragment : Fragment(R.layout.fragment_view_diary_logs)
{
	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		applyWindowInsets()
	}
}
