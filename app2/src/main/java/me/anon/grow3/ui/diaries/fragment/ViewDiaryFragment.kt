package me.anon.grow3.ui.diaries.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_view_diary.*
import me.anon.grow3.R
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.activity.DiaryActivity
import me.anon.grow3.ui.main.activity.MainActivity
import me.anon.grow3.util.navigateForResult
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick

class ViewDiaryFragment : BaseFragment(R.layout.fragment_view_diary)
{
	companion object
	{
		public const val EXTRA_DIARY_ID = "diary.id"
	}

	private lateinit var diaryId: String
	override val inject: (ApplicationComponent) -> Unit = {}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)
		activity?.actionBar?.hide()
	}

	override fun bindArguments(bundle: Bundle?)
	{
		if (arguments?.getString(EXTRA_DIARY_ID).isNullOrBlank())
		{
			empty_view.isVisible = true
			diary_details.isVisible = false
			new_diary.onClick {
				navigateForResult<DiaryActivity>()
			}
		}
		else
		{
			empty_view.isVisible = false
			diary_details.isVisible = true
			requireActivity().title = "Diary"
			diaryId = arguments?.getString(EXTRA_DIARY_ID)!!
		}
	}

	override fun bindUi()
	{
		crop1.onClick {
			navigateTo<MainActivity> {
				putExtras(bundleOf(MainActivity.EXTRA_NAVIGATE to MainActivity.NAVIGATE_TO_CROPS))
				putExtras(bundleOf(MainActivity.EXTRA_DIARY_ID to diaryId))
			}
		}
	}

	override fun bindVm()
	{

	}
}
