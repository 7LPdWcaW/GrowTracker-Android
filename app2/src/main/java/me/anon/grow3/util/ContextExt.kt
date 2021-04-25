package me.anon.grow3.util

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.anon.grow3.BaseApplication

public val Context.application get() = applicationContext as BaseApplication
public val Context.component get() = application.appComponent

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
