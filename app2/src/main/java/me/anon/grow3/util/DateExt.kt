package me.anon.grow3.util

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

/**
 * Date util class for formatting/converting strings into date objects
 */
object DateUtils
{
	const val TIME_FORMAT = "HH:mm"
	const val CLOCK_FORMAT = "HH:mm:ss"
	const val DATE_FORMAT = "dd/MM/yyyy"
}

/**
 * Parses the given string as ISO-8601
 */
public fun String.asLocalDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME)

/**
 * Parses the given string as ISO-8601
 */
public fun String.asDateTime(): ZonedDateTime = ZonedDateTime.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME)

/**
 * Converts local date time to string format
 */
public fun ZonedDateTime.asString(): String = format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

public fun ZonedDateTime.asFormattedString(): String = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

/**
 * Parses the given string as HH:mm(:ss) format
 */
public fun String.asLocalTime(): LocalTime
{
	if (length > DateUtils.TIME_FORMAT.length)
	{
		return LocalTime.parse(this, DateTimeFormatter.ofPattern(DateUtils.CLOCK_FORMAT))
	}

	return LocalTime.parse(this, DateTimeFormatter.ofPattern(DateUtils.TIME_FORMAT))
}
