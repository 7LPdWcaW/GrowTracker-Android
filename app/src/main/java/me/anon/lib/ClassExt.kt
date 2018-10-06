package me.anon.lib

import android.app.Activity
import com.kenny.snackbar.SnackBar
import com.kenny.snackbar.SnackBarListener

fun SnackBar.show(
	context: Activity,
	message: Int, action: Int,
	startListener: () -> kotlin.Unit,
	endListener: () -> kotlin.Unit,
	actionListener: () -> kotlin.Unit
)
{
	SnackBar.show(context, message, action, object : SnackBarListener
	{
		override fun onSnackBarStarted(`object`: Any?)
		{
			startListener.invoke()
		}

		override fun onSnackBarAction(`object`: Any?)
		{
			actionListener.invoke()
		}

		override fun onSnackBarFinished(`object`: Any?)
		{
			endListener.invoke()
		}
	})
}
