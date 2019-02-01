package me.anon.lib

import android.app.Activity
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar

abstract class SnackBarListener
{
	abstract fun onSnackBarStarted(o: Any)
	abstract fun onSnackBarFinished(o: Any)
	abstract fun onSnackBarAction(o: Any)
}

/**
 * Snackbar helper class
 */
class SnackBar
{
	companion object
	{
		@JvmStatic
		public fun show(context: Activity, @StringRes messageRes: Int, @StringRes actionTextRes: Int = -1,
			listener: SnackBarListener
		)
		{
			show(context, context.getString(messageRes),
				if (actionTextRes != -1) context.getString(actionTextRes) else "",
				listener
			)
		}

		@JvmStatic
		public fun show(context: Activity, message: String, listener: SnackBarListener)
		{
			show(context, message, "", listener)
		}

		@JvmStatic
		public fun show(context: Activity, message: String, actionText: String = "",
			listener: SnackBarListener
		)
		{
			SnackBar().show(context, message, actionText, {
				listener.onSnackBarStarted(0)
			}, {
				listener.onSnackBarFinished(0)
			}, {
				listener.onSnackBarAction(0)
			})
		}
	}

	public fun show(context: Activity, @StringRes messageRes: Int)
	{
		show(context, messageRes)
	}

	public fun show(context: Activity, @StringRes messageRes: Int, @StringRes actionTextRes: Int = -1,
		start: () -> kotlin.Unit = {},
		end: () -> kotlin.Unit = {},
		action: () -> kotlin.Unit = {}
	)
	{
		show(context, context.getString(messageRes),
			if (actionTextRes != -1) context.getString(actionTextRes) else "",
			start, end, action
		)
	}

	public fun show(context: Activity, message: String, actionText: String = "",
		start: () -> kotlin.Unit = {},
		end: () -> kotlin.Unit = {},
		action: () -> kotlin.Unit = {}
	)
	{
		val snackbar = Snackbar.make(context.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)

		if (actionText.isNotEmpty())
		{
			snackbar.setAction(actionText) { _ ->
				action.invoke()
			}
		}

		snackbar.setCallback(object : Snackbar.Callback()
		{
			override fun onShown(sb: Snackbar?)
			{
				start.invoke()
			}

			override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
			{
				end.invoke()
			}
		})

		snackbar.show()
	}
}
