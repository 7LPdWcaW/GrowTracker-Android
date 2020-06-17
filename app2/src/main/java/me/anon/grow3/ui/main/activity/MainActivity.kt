package me.anon.grow3.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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
import me.anon.grow3.ui.diaries.fragment.EmptyFragment
import me.anon.grow3.ui.main.fragment.AdditionalPageHostFragment
import me.anon.grow3.ui.main.fragment.MainNavigatorFragment
import me.anon.grow3.ui.main.fragment.NavigationFragment
import me.anon.grow3.util.nameOf

class MainActivity : BaseActivity(ActivityMainBinding::class.java)
{
	companion object
	{
		const val INDEX_MENU = 0
		const val INDEX_MAIN = 1
		const val INDEX_NAVSTACK = 2

		const val EXTRA_ORIGINATOR = "origin"
		const val EXTRA_NAVIGATE = "navigation"

		const val EXTRA_DIARY_ID = "diary.id"
		const val EXTRA_CROP_ID = "crop.id"
		const val EXTRA_LOG_ID = "log.id"
	}

	inner class PageAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(supportFragmentManager, lifecycle)
	{
		public val pages = arrayListOf<Fragment>()

		override fun getItemCount(): Int = pages.size
		override fun createFragment(position: Int): Fragment = pages[position]
	}

	private val adapter by lazy { PageAdapter(supportFragmentManager, lifecycle) }
	private val viewBindings by viewBinding<ActivityMainBinding>()
	public val viewPager get() = viewBindings.viewPager

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		if (savedInstanceState == null)
		{
			adapter.pages.apply {
				add(INDEX_MENU, NavigationFragment())
				add(INDEX_MAIN, MainNavigatorFragment().apply {
					arguments = bundleOf(EXTRA_NAVIGATE to nameOf<EmptyFragment>())
				})
			}
		}
		else
		{
			val navFragment = supportFragmentManager.fragments.first { it is NavigationFragment }
			val mainFragment = supportFragmentManager.fragments.first { it is MainNavigatorFragment }
			adapter.pages.apply {
				add(INDEX_MENU, navFragment)
				add(INDEX_MAIN, mainFragment)

				if (supportFragmentManager.fragments.size > 2)
				{
					for (index in 2 until supportFragmentManager.fragments.size)
					{
						add(supportFragmentManager.fragments[index])
					}
				}
			}
		}
	}

	override fun onPostCreate(savedInstanceState: Bundle?)
	{
		super.onPostCreate(savedInstanceState)
		val position = savedInstanceState?.getInt("state.viewpager_position", INDEX_MAIN) ?: INDEX_MAIN
		viewBindings.viewPager.setCurrentItem(position, false)
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		super.onSaveInstanceState(outState)
		outState.putInt("state.viewpager_position", viewPager.currentItem)
	}

	override fun onNewIntent(intent: Intent?)
	{
		super.onNewIntent(intent)
		adapter.pages[INDEX_MAIN].apply { arguments = intent?.extras }
	}

	override fun bindUi()
	{
		viewBindings.viewPager.adapter = adapter
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

	public fun addToStack(fragment: Fragment)
	{
		val pageHost = AdditionalPageHostFragment()
		adapter.pages.add(pageHost)
		pageHost.addPage(fragment)
		notifyPagerChange(pageHost)
	}

	public fun clearStack()
	{
		viewBindings.viewPager.setCurrentItem(INDEX_MAIN, true)
		val count = adapter.pages.size
		if (count > INDEX_MAIN + 1)
		{
			while (adapter.pages.size > INDEX_MAIN + 1)
			{
				adapter.pages.removeAt(INDEX_MAIN + 1)
			}

			adapter.notifyItemRangeRemoved(INDEX_MAIN + 1, count - 1)
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
			INDEX_MAIN -> {
				if (!(adapter.pages[INDEX_MAIN] as BaseHostFragment).onBackPressed()) super.onBackPressed()
			}
			else -> {
				if (!(adapter.pages[viewBindings.viewPager.currentItem] as BaseHostFragment).onBackPressed())
				{
					val index = viewBindings.viewPager.currentItem
					val callback = object : ViewPager2.OnPageChangeCallback()
					{
						override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
						{
							if (position < index && positionOffsetPixels == 0)
							{
								// remove all pages from current to end of adapter
								val size = adapter.pages.size
								while (adapter.pages.size > index)
								{
									adapter.pages.removeAt(index)
								}

								adapter.notifyItemRangeRemoved(index, size - index)
								viewBindings.viewPager.unregisterOnPageChangeCallback(this)
							}
						}
					}
					viewBindings.viewPager.registerOnPageChangeCallback(callback)
					viewBindings.viewPager.currentItem = index - 1
				}
			}
		}
	}
}
