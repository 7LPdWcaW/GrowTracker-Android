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
import me.anon.grow3.util.dp
import me.anon.grow3.util.parentView
import timber.log.Timber

class MainActivity : BaseActivity()
{
	var lastPosition = -1f
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)

		view_pager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle)
		{
			override fun getItemCount(): Int = 3

			override fun createFragment(position: Int): Fragment
			{
				return if (position == 0) DiariesListFragment() else TestFragment()
			}
		}
		view_pager.setCurrentItem(1, false)

		view_pager.setPageTransformer { page, position ->
			page.translationZ = 0f

//			val content = findViewById<View>(R.id.content)
//			if (position < 1)
//			{
//				val border = 0
//				val speed = -0.2f
//				val width = content.getWidth()
//				content.setTranslationX(-(position * width * speed))
//				val sc = (page.getWidth() - border) / page.getWidth()
//				if (position == 0f)
//				{
//					page.setScaleX(1f)
//					page.setScaleY(1f)
//				}
//				else
//				{
//					page.setScaleX(sc.toFloat())
//					page.setScaleY(sc.toFloat())
//					page.translationZ = 100f
//				}
//			}
			val index = (page.parent as ViewGroup).indexOfChild(page)
			page.findViewById<View?>(R.id.fade_overlay)?.apply {
				setOnClickListener(null)
			}

			when
			{
				position < -1 -> {
					page.alpha = 1f
					//page.translationZ = 100f
				}
				position < 0 -> {

					if (index == 0)
					{
						page.translationZ = 100f
						page.elevation = 100f
						page.x = (page.width / 1f) * position// + (150f.dp(page.context))

						page.findViewById<View?>(R.id.menu_view)?.let {
							Timber.e("${page.x} ${page.parentView.x} ${position}")
							if (page.x >= -it.paddingStart)
							{
								page.x = page.x.coerceAtMost(-it.paddingStart.toFloat())
							}
							else
							{
							}
						}
					}
//					else if (index == 1)
//					{
//						page.x = 0f
//					}
				}
				position <= 1 -> {
					page.findViewById<View?>(R.id.fade_overlay)?.apply {
						alpha = position.coerceAtMost(0.7f)
						isVisible = alpha > 0f
					}

					if (index != 0)
//					{
//						page.findViewById<View?>(R.id.menu_view)?.let {
//							page.x = page.x.coerceAtMost(-it.paddingStart.toFloat())
//						}
//					}
//					else
					{
						page.findViewById<View?>(R.id.fade_overlay)?.apply {
							setOnClickListener {
								view_pager.currentItem = 1
							}
						}

						page.x = (page.width * position).coerceAtMost(page.width - 150f.dp(page.context))
					}
				}
				else -> {
					//page.translationZ = 100f
				}
			}
		}
	}
}
