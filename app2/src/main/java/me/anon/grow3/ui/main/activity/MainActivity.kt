package me.anon.grow3.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freelapp.flowlifecycleobserver.collectWhileResumed
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import me.anon.grow3.R
import me.anon.grow3.data.event.LogEvent
import me.anon.grow3.databinding.ActivityMainBinding
import me.anon.grow3.ui.base.BaseActivity
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.base.BaseHostFragment
import me.anon.grow3.ui.main.fragment.AdditionalPageHostFragment
import me.anon.grow3.ui.main.fragment.MainNavigatorFragment
import me.anon.grow3.ui.main.fragment.NavigationFragment
import me.anon.grow3.ui.main.viewmodel.MainViewModel
import me.anon.grow3.util.*
import timber.log.Timber
import javax.inject.Inject


class MainActivity : BaseActivity(ActivityMainBinding::class)
{
	companion object
	{
		const val INDEX_MENU = 0
		const val INDEX_MAIN = 1
		const val INDEX_NAVSTACK = 2

		const val EXTRA_ORIGINATOR = "origin"
		const val EXTRA_NAVIGATE = "navigation"
	}

	open class FragmentInstance(
		open var fragment: Class<out Fragment> = BaseFragment::class.java,
		var args: Bundle? = null
	)
	{
		open val id: Long = args?.let { codeOf(it).toLong() } ?: -1L
		open fun newInstance(): BaseFragment = fragment.newInstance().apply {
			arguments = args
		} as BaseFragment

		public fun saveState(): Bundle = Bundle().apply {
			args?.let { putAll(it) }
			putLong("state.id", id)
			putString("state.fragment", fragment.name)
		}

		public fun restoreState(bundle: Bundle)
		{
			//id = bundle.getLong("state.id")
			bundle.remove("state.id")

			fragment = Class.forName(bundle.getString("state.fragment")!!) as Class<out BaseFragment>
			bundle.remove("state.fragment")
			args = bundle
		}
	}

	private class PageAdapter(val activity: AppCompatActivity) : FragmentStateAdapter(activity)
	{
		public val pages = arrayListOf<FragmentInstance>().apply {
			add(INDEX_MENU, object : FragmentInstance(NavigationFragment::class.java)
			{
				override val id: Long
					get() = codeOf<NavigationFragment>().toLong()
			})
			add(INDEX_MAIN, object : FragmentInstance(
				MainNavigatorFragment::class.java,
				activity.intent?.extras
			)
			{
				override val id: Long
					get() = codeOf<MainNavigatorFragment>().toLong()
			})
		}

		override fun getItemCount(): Int = pages.size
		override fun createFragment(position: Int): Fragment = pages[position].newInstance().apply {
			arguments = pages[position].args
		}
		override fun getItemId(position: Int): Long = pages[position].id
		override fun containsItem(itemId: Long): Boolean = pages.any { it.id == itemId }
		public fun getFragment(position: Int): Fragment? = activity.supportFragmentManager.findFragmentByTag("f" + getItemId(position))
	}

	private val adapter by lazy { PageAdapter(this) }
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: MainViewModel.Factory
	private val viewModel: MainViewModel by viewModels { ViewModelProvider(viewModelFactory, this, intent.extras) }
	public val viewBindings by viewBinding<ActivityMainBinding>()
	public val viewPager get() = viewBindings.viewPager

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		if (savedInstanceState != null)
		{
			savedInstanceState.getBundle("state.adapter")?.let { adapterState ->
				adapterState.keySet().forEach { key ->
					val index = key.split(".").last().toInt()
					if (index > INDEX_MAIN)
					{
						adapter.pages.add(index, FragmentInstance().apply {
							restoreState(adapterState.getBundle(key)!!)
						})
					}
				}
			}

			adapter.notifyDataSetChanged()
		}

		if (intent.extras != null)
		{
			// new intent
			onNewIntent(intent)
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

		val adapterState = Bundle()
		adapter.pages.map { it.saveState() }.forEachIndexed { index, page ->
			adapterState.putBundle("state.adapter.$index", page)
		}
		outState.putBundle("state.adapter", adapterState)
	}

	override fun onNewIntent(intent: Intent?)
	{
		super.onNewIntent(intent)

		supportFragmentManager.findFragmentByTag("f" + adapter.getItemId(INDEX_MAIN).toInt())?.let {
			it.arguments = intent?.extras
		}
	}

	override fun bindVm()
	{
		viewModel.logEvents.collectWhileResumed(this) { event ->
			when (event)
			{
				is LogEvent.Added -> {
					Timber.e(event.log.toJsonString())
					Toast.makeText(this, "${event.log} added to ${event.diary.name}", Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	override fun bindUi()
	{
		val layoutSheetBehavior = BottomSheetBehavior.from(viewBindings.bottomSheet)
		layoutSheetBehavior.state = STATE_HIDDEN
		layoutSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback()
		{
			override fun onSlide(bottomSheet: View, slideOffset: Float)
			{
			}

			override fun onStateChanged(bottomSheet: View, newState: Int)
			{
				adapter.getFragment(INDEX_MENU)?.let {
					it.requireView().updatePadding(bottom = insets.value?.bottom ?: 0)
					if (newState == STATE_COLLAPSED)
					{
						it.requireView().updatePadding(bottom = layoutSheetBehavior.peekHeight - (insets.value?.bottom ?: 0))
					}
				}
			}
		})

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

	public fun openSheet(fragment: Fragment)
	{
		val sheetBehavior = BottomSheetBehavior.from(viewBindings.bottomSheet)
		sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback()
		{
			override fun onSlide(bottomSheet: View, slideOffset: Float){}

			override fun onStateChanged(bottomSheet: View, newState: Int)
			{
				if (newState == STATE_HIDDEN)
				{
					supportFragmentManager.findFragmentById(R.id.bottom_sheet)?.let {
						supportFragmentManager.commit {
							remove(it)
						}

						sheetBehavior.removeBottomSheetCallback(this)
					}
				}
			}
		})

		supportFragmentManager.commitNow {
			replace(R.id.bottom_sheet, fragment)
		}
	}

	public fun addToStack(fragment: Class<out BaseFragment>, _args: Bundle?)
	{
		val index = adapter.pages.size
		val transaction = Bundle().apply {
			_args?.let { putAll(it) }
			putString(EXTRA_NAVIGATE, fragment.name)
		}

		adapter.pages.add(index, object : FragmentInstance(AdditionalPageHostFragment::class.java, transaction)
		{
			override fun newInstance(): BaseFragment = super.newInstance().apply {
				lifecycleScope.launchWhenCreated {
					viewBindings.viewPager.post {
						viewBindings.viewPager.setCurrentItem(index, true)
					}
				}
			}
			override val id: Long get() = codeOf<AdditionalPageHostFragment>().toLong() + codeOf(args!!)
		})

		adapter.notifyItemInserted(index)
	}

	public fun clearStack(now: Boolean = false)
	{
		// only remove stack if there is a stack to remove
		val index = viewBindings.viewPager.currentItem
		if (adapter.pages.size - 1 > INDEX_MAIN)
		{
			fun removePages()
			{
				val count = adapter.pages.size
				if (count > INDEX_MAIN + 1)
				{
					while (adapter.pages.size > INDEX_MAIN + 1)
					{
						supportFragmentManager.commitNow {
							adapter.getFragment(INDEX_MAIN + 1)?.let {
								remove(it)
							}
						}

						adapter.pages.removeAt(INDEX_MAIN + 1)
						adapter.notifyItemRemoved(INDEX_MAIN + 1)
					}

					viewBindings.viewPager.forceLayout()
					viewBindings.viewPager.invalidate()
				}
			}

			if (now)
			{
				removePages()
			}
			else
			{
				val callback = object : ViewPager2.OnPageChangeCallback()
				{
					override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
					{
						if (position <= index && positionOffsetPixels <= 1)
						{
							removePages()
							viewBindings.viewPager.unregisterOnPageChangeCallback(this)
						}
					}
				}

				viewBindings.viewPager.registerOnPageChangeCallback(callback)
			}

			viewBindings.viewPager.setCurrentItem(INDEX_MAIN, !now)
		}
	}

	public fun notifyPagerChange(index: Int)
	{
		adapter.notifyItemChanged(index)
		viewBindings.viewPager.forceLayout()
		viewBindings.viewPager.post {
			viewBindings.viewPager.setCurrentItem(index, true)
		}
	}

	override fun onBackPressed()
	{
		supportFragmentManager.findFragmentById(R.id.bottom_sheet)?.let {
			if ((it as? BaseFragment)?.onBackPressed() == true) return@onBackPressed
		}

		when (viewBindings.viewPager.currentItem)
		{
			INDEX_MENU -> viewBindings.viewPager.currentItem = INDEX_MAIN
			INDEX_MAIN -> {
				if (!(adapter.getFragment(INDEX_MAIN) as BaseHostFragment).onBackPressed())
				{
					// also check in case the bottom sheet is collapsed!
					supportFragmentManager.findFragmentById(R.id.bottom_sheet)?.let {
						val layoutSheetBehavior = BottomSheetBehavior.from(viewBindings.bottomSheet)
						if (layoutSheetBehavior.state == STATE_COLLAPSED)
						{
							promptExit {
								super.onBackPressed()
							}

							return@onBackPressed
						}
					}

					super.onBackPressed()
				}
			}
			else -> {
				if (!(adapter.getFragment(viewBindings.viewPager.currentItem) as BaseHostFragment).onBackPressed())
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

								viewBindings.viewPager.unregisterOnPageChangeCallback(this)
								viewBindings.viewPager.post { adapter.notifyItemRangeRemoved(index, size - index) }
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
