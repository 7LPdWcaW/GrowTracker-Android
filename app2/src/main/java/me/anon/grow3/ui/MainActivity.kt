package me.anon.grow3.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.diaries.fragment.DiariesListFragment
import me.anon.grow3.ui.diaries.fragment.TestFragment

class MainActivity : BaseActivity()
{
	inner class Adapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(supportFragmentManager, lifecycle)
	{
		public val pages = arrayListOf<Fragment>().apply {
			add(DiariesListFragment())
			add(TestFragment())
		}

		override fun getItemCount(): Int = pages.size
		override fun createFragment(position: Int): Fragment = pages[position]
	}

	val adapter by lazy { Adapter(supportFragmentManager, lifecycle) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)

		view_pager.adapter = adapter

		view_pager.setCurrentItem(1, false)
		view_pager.setPageTransformer { page, position ->
			page.translationZ = 0f
			val translateX = position * page.width

			val index = (page.parent as ViewGroup).indexOfChild(page)
			page.findViewById<View?>(R.id.fade_overlay)?.apply {
				setOnClickListener(null)
			}

			page.findViewById<View?>(R.id.main_content)?.let {
				if (position > -1) it.x = if (-translateX <= 0) 0f else -(translateX * 0.85f)
			}

			when
			{
				position <= 0 -> {
					if (index == 0)
					{
						page.x = translateX

						page.findViewById<View?>(R.id.menu_view)?.let {
							val menuTranslate = -(page.width - (page.width * 0.85f))
							if (page.x >= menuTranslate)
							{
								page.x = page.x.coerceAtMost(menuTranslate)
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

						page.x = translateX.coerceAtMost(page.width * 0.85f)
					}
				}
			}
		}
	}

	override fun onBackPressed()
	{
		if (view_pager.currentItem == 2)
		{
			val callback = object : ViewPager2.OnPageChangeCallback()
			{
				override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
				{
					if (position == 1 && positionOffsetPixels == 0)
					{
						adapter.pages.removeAt(2)
						adapter.notifyItemRemoved(2)
						view_pager.unregisterOnPageChangeCallback(this)
					}
				}
			}
			view_pager.registerOnPageChangeCallback(callback)
			view_pager.currentItem = 1
		}
		else if (view_pager.currentItem == 0)
		{
			view_pager.currentItem = 1
		}
		else
		{
			super.onBackPressed()
		}
	}

	public fun openPage(fragmentManager: FragmentManager, fragment: Fragment)
	{
		if (adapter.itemCount == 2) adapter.pages += fragment
		else
		{
			adapter.pages[2] = fragment
		}

		adapter.notifyItemChanged(2)
		view_pager.setCurrentItem(2, true)
	}
}
