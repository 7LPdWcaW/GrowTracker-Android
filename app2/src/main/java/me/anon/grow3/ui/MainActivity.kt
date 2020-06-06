package me.anon.grow3.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.activity_main.*
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.diaries.fragment.DiariesListFragment
import me.anon.grow3.ui.diaries.fragment.TestFragment
import me.anon.grow3.ui.diaries.fragment.TestFragment2
import timber.log.Timber

class MainActivity : BaseActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)

		view_pager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle)
		{
			override fun getItemCount(): Int = 3

			override fun createFragment(position: Int): Fragment
			{
				return when (position)
				{
					0 -> DiariesListFragment()
					1 -> TestFragment()
					2 -> TestFragment2()
					else -> throw IndexOutOfBoundsException()
				}
			}
		}
		view_pager.setCurrentItem(1, false)

		view_pager.setPageTransformer { page, position ->
			page.translationZ = 0f

			val index = (page.parent as ViewGroup).indexOfChild(page)
			page.findViewById<View?>(R.id.fade_overlay)?.apply {
				setOnClickListener(null)
			}

			page.findViewById<View?>(R.id.main_content)?.let {
				if (position > -1)// && position < 1)
				{
					val width = it.width
					val translateX = -(position * width * 0.2f)
					if (translateX < 0)
					{
						it.x = 0f
					}
					else
					{
						it.x = translateX
					}

					Timber.e("${-(position * width * 0.2f)}")
				}
			}

			when
			{
				position < -1 -> {
				}
				position <= 0 -> {
					if (index == 0)
					{
						page.x = (page.width / 1f) * position

						page.findViewById<View?>(R.id.menu_view)?.let {
							if (page.x >= -(page.width - (page.width * 0.85f)))
							{
								page.x = page.x.coerceAtMost(-(page.width - (page.width * 0.85f)))
							}
						}
					}
				}
				position <= 1 -> {
					page.findViewById<View?>(R.id.fade_overlay)?.apply {
						alpha = position.coerceAtMost(0.7f)
						isVisible = alpha >= 0.1f

						setOnClickListener {
							view_pager.currentItem = 1
						}

						page.x = (page.width * position).coerceAtMost(page.width * 0.85f)
					}
				}
				else -> {
				}
			}
		}
	}
}
