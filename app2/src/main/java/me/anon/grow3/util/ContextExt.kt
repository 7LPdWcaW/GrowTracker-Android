package me.anon.grow3.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.anon.grow3.BaseApplication
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.main.activity.MainActivity

public val Context.application get() = applicationContext as BaseApplication
public val Context.component get() = application.appComponent

public inline fun <reified T : BaseFragment> Context.navigateTo(ontop: Boolean = false, arguments: () -> Bundle? = { null })
	= startActivity(Intent(this, MainActivity::class.java).apply {
		if (!ontop)
		{
			putExtra(MainActivity.EXTRA_ORIGINATOR, this@navigateTo::class.java.name)
		}

		putExtra(MainActivity.EXTRA_NAVIGATE, T::class.java.name)
		arguments()?.let { putExtras(it) }
	})

public inline fun Context.promptExit(crossinline callback: () -> Unit)
{
	if (this !is Activity && this !is Fragment) return

	MaterialAlertDialogBuilder(this)
		.setTitle("Are you sure?")
		.setMessage("You will lose any unsaved changes")
		.setPositiveButton("Quit") { dialog, which ->
			callback()
		}
		.setNegativeButton("Cancel", null)
		.show()
}
