package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_view_diary.*
import me.anon.grow3.R
import me.anon.grow3.ui.MainActivity

class ViewDiaryFragment : Fragment(R.layout.fragment_view_diary)
{
	companion object
	{
		public const val EXTRA_DIARY_ID = "diary.id"
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		navigate.setOnClickListener {
			(activity as MainActivity).openPage(TestFragment())
		}
	}
}
