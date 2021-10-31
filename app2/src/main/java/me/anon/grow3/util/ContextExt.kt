package me.anon.grow3.util

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.anon.grow3.BaseApplication

public val Context.application get() = applicationContext as BaseApplication
public val Context.component get() = BaseApplication.appComponent

public inline fun Context.promptExit(crossinline callback: (Boolean) -> Unit)
{
	if (this !is Activity) return

	MaterialAlertDialogBuilder(this)
		.setTitle("Are you sure?")
		.setMessage("You will lose any unsaved changes")
		.setPositiveButton("Quit") { dialog, which ->
			callback(true)
		}
		.setNegativeButton("Cancel") { dialog, which ->
			callback(false)
		}
		.setOnCancelListener {
			callback(false)
		}
		.setNegativeButton("Cancel", null)
		.show()
}

public inline fun Fragment.promptRemove(crossinline callback: (Boolean) -> Unit)
{
	requireActivity().promptRemove(callback)
}

public inline fun Context.promptRemove(crossinline callback: (Boolean) -> Unit)
{
	if (this !is Activity) return

	MaterialAlertDialogBuilder(this)
		.setTitle("Are you sure?")
		.setMessage("Delete the selected item?")
		.setPositiveButton("Delete") { dialog, which ->
			callback(true)
		}
		.setNegativeButton("Cancel") { dialog, which ->
			callback(false)
		}
		.setOnCancelListener {
			callback(false)
		}
		.show()
}
