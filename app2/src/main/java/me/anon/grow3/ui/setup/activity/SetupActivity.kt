package me.anon.grow3.ui.setup.activity

import android.Manifest
import android.os.Bundle
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import me.anon.grow3.R
import me.anon.grow3.util.resolve

class SetupActivity : AppIntro2()
{
	private val colors by lazy {
		arrayOf(
			R.color.colorPrimary.resolve(this),
			R.color.colorAccent.resolve(this),
			R.color.colorSecondary.resolve(this)
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
		addSlide(AppIntroFragment.newInstance(
			title = "Welcome",
			description = "Third screen",
			backgroundColor = colors[2]
		))

		isColorTransitionsEnabled = true
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
	}

	override fun onPageSelected(position: Int)
	{
		super.onPageSelected(position)
		setStatusBarColor(colors[position])
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
