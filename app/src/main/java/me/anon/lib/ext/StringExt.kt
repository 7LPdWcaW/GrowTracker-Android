package me.anon.lib.ext

import android.graphics.Typeface
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan

/**
 * // TODO: Add class description
 */
public fun String.bolden(): CharSequence
{
	return SpannableString(this).also {
		it.setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
	}
}

public fun String.asHtml(): Spanned = Html.fromHtml(this)
