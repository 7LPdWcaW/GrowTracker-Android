package me.anon.grow3.util

import android.text.Editable
import android.text.SpannableStringBuilder
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

public fun String.asEditable(): Editable = SpannableStringBuilder(this)

public fun String.nowDifferenceDays(): Int = (this and ZonedDateTime.now().asString()).dateDifferenceDays()

public fun Duo<String>.dateDifferenceDays(): Int
{
	return try
	{
		ChronoUnit.DAYS.between(first.asDateTime(), second!!.asDateTime()).toInt()
	}
	catch (e: Exception)
	{
		0
	}
}
