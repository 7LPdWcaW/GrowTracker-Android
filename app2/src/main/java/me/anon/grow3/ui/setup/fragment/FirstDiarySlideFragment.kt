package me.anon.grow3.ui.setup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import me.anon.grow3.R
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.util.color
import me.anon.grow3.util.newTask

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

		view.findViewById<View>(R.id.new_diary).setOnClickListener {
			newTask<DiaryActivity>()
			requireActivity().finish()
		}
	}

	override val defaultBackgroundColor: Int
		get() = R.color.colorTertiary.color(requireContext())

	override fun setBackgroundColor(backgroundColor: Int)
	{
		requireView().findViewById<View>(R.id.content).setBackgroundColor(backgroundColor)
	}
}
