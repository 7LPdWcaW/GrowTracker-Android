package me.anon.grow3.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import me.anon.grow3.R
import me.anon.grow3.databinding.ActivityMainBinding
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.diaries.fragment.DiariesListFragment
import me.anon.grow3.ui.main.fragment.AdditionalPageHostFragment
import me.anon.grow3.ui.main.fragment.MainNavigationFragment

class MainActivity : BaseActivity(ActivityMainBinding::class.java)
{
	companion object
	{
		const val INDEX_MENU = 0
		const val INDEX_MAIN = 1
		const val INDEX_NAVSTACK = 2

		const val EXTRA_NAVIGATE = "navigation"
		const val NAVIGATE_TO_DIARY = "open.diary"
		const val NAVIGATE_TO_CROPS = "open.diary.crops"
		const val EXTRA_DIARY_ID = "diary.id"
	}

	inner class PageAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(supportFragmentManager, lifecycle)
	{
		public val pages = arrayListOf<Fragment>().apply {
			add(INDEX_MENU, DiariesListFragment())
			add(INDEX_MAIN, MainNavigationFragment().apply {
				arguments = intent.extras
			})
		}

		override fun getItemCount(): Int = pages.size
		override fun createFragment(position: Int): Fragment = pages[position]
	}

	private val adapter by lazy { PageAdapter(supportFragmentManager, lifecycle) }
	private val viewBindings by lazy { binding<ActivityMainBinding>() }
	public val viewPager by lazy { viewBindings.viewPager }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		bindUi()
	}

	override fun onNewIntent(intent: Intent?)
	{
		super.onNewIntent(intent)
		adapter.pages[INDEX_MAIN].apply { arguments = intent?.extras }
	}

	private fun bindUi()
	{
		viewBindings.viewPager.adapter = adapter
		viewBindings.viewPager.setCurrentItem(INDEX_MAIN, false)
		viewBindings.viewPager.offscreenPageLimit = 3
		viewBindings.viewPager.setPageTransformer { page, position ->
			val translateX = position * page.width
			val index = (page.parent as ViewGroup).indexOfChild(page)

			page.translationZ = 0f
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
							viewBindings.viewPager.currentItem = INDEX_MAIN
						}

						page.x = translateX.coerceAtMost(page.width * 0.85f)
					}
				}
			}
		}
	}

	public fun setDetail(fragment: Fragment?)
	{
		if (fragment == null)
		{
			if (adapter.pages.size == 3)
			{
				adapter.pages.removeAt(INDEX_NAVSTACK)
				adapter.notifyItemRemoved(INDEX_NAVSTACK)
				viewBindings.viewPager.setCurrentItem(INDEX_MAIN, true)
			}
		}
		else
		{
			if (adapter.pages.size == 2) adapter.pages.add(INDEX_NAVSTACK, AdditionalPageHostFragment())
			val pageHost = adapter.pages[INDEX_NAVSTACK] as AdditionalPageHostFragment
			pageHost.addPage(fragment)
			notifyPagerChange(pageHost)
		}
	}

	public fun notifyPagerChange(fragment: BaseHostFragment)
	{
		viewBindings.viewPager.post {
			adapter.notifyItemChanged(adapter.pages.indexOf(fragment))
			viewBindings.viewPager.forceLayout()
			viewBindings.viewPager.setCurrentItem(adapter.pages.indexOf(fragment), true)
		}
	}

	override fun onBackPressed()
	{
		when (viewBindings.viewPager.currentItem)
		{
			INDEX_MENU -> viewBindings.viewPager.currentItem = INDEX_MAIN
			INDEX_NAVSTACK -> {
				if (!(adapter.pages[INDEX_NAVSTACK] as BaseHostFragment).onBackPressed())
				{
					val callback = object : ViewPager2.OnPageChangeCallback()
					{
						override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
						{
							if (position == INDEX_MAIN && positionOffsetPixels == 0)
							{
								adapter.pages.removeAt(INDEX_NAVSTACK)
								adapter.notifyItemRemoved(INDEX_NAVSTACK)
								viewBindings.viewPager.unregisterOnPageChangeCallback(this)
							}
						}
					}
					viewBindings.viewPager.registerOnPageChangeCallback(callback)
					viewBindings.viewPager.currentItem = INDEX_MAIN
				}
			}
			else -> {
				if (!(adapter.pages[INDEX_MAIN] as BaseHostFragment).onBackPressed()) super.onBackPressed()
			}
		}
	}
}
