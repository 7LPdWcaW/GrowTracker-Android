package me.anon.grow3.ui.setup.activity

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import me.anon.grow3.R
import me.anon.grow3.ui.setup.fragment.FirstDiarySlideFragment
import me.anon.grow3.util.resolve

class SetupActivity : AppIntro2()
{
	override val layoutId = R.layout.activity_setup

	private val colors by lazy {
		arrayOf(
			R.color.colorPrimary.resolve(this),
			R.color.colorSecondary.resolve(this),
			R.color.colorAccent.resolve(this)
		)
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		addSlide(AppIntroFragment.newInstance(
			title = "Welcome",
			description = "First screen",
			backgroundColor = colors[0]
		))
		addSlide(AppIntroFragment.newInstance(
			title = "Welcome",
			description = "Second screen",
			backgroundColor = colors[1]
		))
		addSlide(FirstDiarySlideFragment())

		isColorTransitionsEnabled = true

		setStatusBarColor(0x00ffffff)
		showStatusBar(true)

		isIndicatorEnabled = true
		setIndicatorColor(
			selectedIndicatorColor = 0xffcc0000.toInt(),
			unselectedIndicatorColor = 0xff333333.toInt()
		)

		askForPermissions(
			permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
			slideNumber = 2,
			required = true
		)

		findViewById<View>(R.id.bottom).setOnApplyWindowInsetsListener { view, windowInsets ->
			view.onApplyWindowInsets(windowInsets).also {
				view.updateLayoutParams<ConstraintLayout.LayoutParams> {
					bottomMargin = windowInsets.systemWindowInsetBottom
				}
			}
		}
	}

	override fun onUserDeniedPermission(permissionName: String)
	{
		// User pressed "Deny" on the permission dialog
	}

	override fun onUserDisabledPermission(permissionName: String)
	{
		// User pressed "Deny" + "Don't ask again" on the permission dialog
	}
}
