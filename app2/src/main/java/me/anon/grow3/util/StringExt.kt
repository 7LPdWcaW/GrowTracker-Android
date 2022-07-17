package me.anon.grow3.util

import android.text.Editable
import android.text.SpannableStringBuilder
import org.threeten.bp.temporal.ChronoUnit

public fun String.asEditable(): Editable = SpannableStringBuilder(this)
public fun String.nowDifferenceDays(): Int = (this and DateUtils.newApiDateString()).dateDifferenceDays()

/**
 * Returns the difference of 2 date strings in whole days. This is inclusive of the current
 * day (i.e the "first" day rather than "one whole day"
 */
public fun Duo<String>.dateDifferenceDays(): Int
{
	return try
	{
		ChronoUnit.DAYS.between(first.asDateTime(), second!!.asDateTime()).toInt() + 1
	}
	catch (e: Exception)
	{
		1
	}
}
