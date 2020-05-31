package me.anon.grow3.ui.setup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import kotlinx.android.synthetic.main.fragment_intro_slide_diary.*
import me.anon.grow3.R
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.util.color
import me.anon.grow3.util.navigateTo

class FirstDiarySlideFragment : Fragment(), SlideBackgroundColorHolder
{
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? = inflater.inflate(R.layout.fragment_intro_slide_diary, container, false)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		new_diary.setOnClickListener {
			navigateTo<DiaryActivity>()
		}
	}

	override val defaultBackgroundColor: Int
		get() = R.color.colorAccent.color(requireContext())

	override fun setBackgroundColor(backgroundColor: Int)
	{
		content.setBackgroundColor(backgroundColor)
	}
}
