package me.anon.grow3.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_host.*
import me.anon.grow3.R
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.diaries.fragment.DiariesListFragment
import me.anon.grow3.ui.diaries.fragment.ViewDiaryFragment


class MainActivity : BaseActivity(R.layout.activity_main)
{
	companion object
	{
		const val INDEX_MENU = 0
		const val INDEX_MAIN = 1
		const val INDEX_NAVSTACK = 2
	}

	class MainHostFragment : Fragment(R.layout.fragment_main_host)
	{
		private val pendingActions = ArrayList<String>(1)

		override fun onActivityCreated(savedInstanceState: Bundle?)
		{
			super.onActivityCreated(savedInstanceState)

			if (pendingActions.isNotEmpty())
			{
				openDiary(pendingActions.removeAt(0))
			}
			else if (savedInstanceState == null)
			{
				childFragmentManager.commit {
					replace(R.id.content, ViewDiaryFragment())
				}
			}

			requireActivity().view_pager.setOnApplyWindowInsetsListener { v, insets ->
				v.onApplyWindowInsets(insets).also {
					val navigationBar = insets.systemWindowInsetBottom
					menu_fab.translationY = (-navigationBar).toFloat()
					sheet.updatePadding(bottom = navigationBar)
				}
			}
		}

		public fun onBackPressed(): Boolean = menu_fab.isExpanded.also { menu_fab.isExpanded = false }

//		public fun openPage(fragment: Fragment, viewPager: ViewPager2)
//		{
//			if (adapter.itemCount == 2) adapter.pages.add(INDEX_NAVSTACK, fragment)
//			else adapter.pages[INDEX_NAVSTACK] = fragment
//
//			adapter.notifyItemChanged(INDEX_NAVSTACK)
//			viewPager.setCurrentItem(INDEX_NAVSTACK, true)
//		}

		public fun openDiary(id: String)
		{
			if (!isAdded || isDetached)
			{
				pendingActions += id
				return
			}

			val fragment = ViewDiaryFragment().apply {
				arguments = Bundle().apply {
					putString(ViewDiaryFragment.EXTRA_DIARY_ID, id)
				}
			}

			childFragmentManager.commit {
				setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
				replace(R.id.content, fragment)
			}

			(activity as MainActivity).view_pager.apply {
				post {
					adapter!!.notifyItemChanged(INDEX_MAIN)
					forceLayout()
					setCurrentItem(INDEX_MAIN, true)
				}
			}

			menu_fab.isVisible = true
			menu_fab.setOnClickListener { menu_fab.isExpanded = !menu_fab.isExpanded }
			sheet.setOnClickListener { menu_fab.isExpanded = false }
		}
	}

	inner class PageAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(supportFragmentManager, lifecycle)
	{
		public val pages = arrayListOf<Fragment>().apply {
			add(INDEX_MENU, DiariesListFragment())
			add(INDEX_MAIN, MainHostFragment())
		}

		override fun getItemCount(): Int = pages.size
		override fun createFragment(position: Int): Fragment = pages[position]
	}

	private val adapter by lazy { PageAdapter(supportFragmentManager, lifecycle) }

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		bindUi()

		if (intent.extras?.containsKey("diary.id") == true)
		{
			openDiary(intent.extras!!["diary.id"] as String)
		}
	}

	private fun bindUi()
	{
		view_pager.adapter = adapter
		view_pager.setCurrentItem(INDEX_MAIN, false)
		view_pager.offscreenPageLimit = 3
		view_pager.setPageTransformer { page, position ->
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
							view_pager.currentItem = INDEX_MAIN
						}

						page.x = translateX.coerceAtMost(page.width * 0.85f)
					}
				}
			}
		}
	}

	override fun onBackPressed()
	{
		when (view_pager.currentItem)
		{
			INDEX_MENU -> view_pager.currentItem = INDEX_MAIN
			INDEX_NAVSTACK -> {
				val callback = object : ViewPager2.OnPageChangeCallback()
				{
					override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
					{
						if (position == INDEX_MAIN && positionOffsetPixels == 0)
						{
							adapter.pages.removeAt(INDEX_NAVSTACK)
							adapter.notifyItemRemoved(INDEX_NAVSTACK)
							view_pager.unregisterOnPageChangeCallback(this)
						}
					}
				}
				view_pager.registerOnPageChangeCallback(callback)
				view_pager.currentItem = INDEX_MAIN
			}
			else -> {
				if (!(adapter.pages[INDEX_MAIN] as MainHostFragment).onBackPressed()) super.onBackPressed()
			}
		}
	}

	public fun openDiary(id: String)
	{
		(adapter.pages[INDEX_MAIN] as MainHostFragment).openDiary(id)
	}
}
