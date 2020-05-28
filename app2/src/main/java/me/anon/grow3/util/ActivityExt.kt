package me.anon.grow3.util

import android.app.Activity
import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder

public inline fun <reified T : Activity> Activity.navigateTo(block: Intent.() -> Unit = {})
	= startActivity(Intent(this, T::class.java).apply(block))

public inline fun Activity.promptExit(crossinline callback: () -> Unit)
{
	MaterialAlertDialogBuilder(this)
		.setTitle("Are you sure?")
		.setMessage("You will lose any unsaved changes")
		.setPositiveButton("Quit") { dialog, which ->
			callback()
		}
		.setNegativeButton("Cancel", null)
		.show()
}
