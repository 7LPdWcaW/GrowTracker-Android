package me.anon.grow3.util

import me.anon.grow3.util.DateUtils.API_FORMAT
import me.anon.grow3.util.DateUtils.DATE_FORMAT
import me.anon.grow3.util.DateUtils.MID_DISPLAY_FORMAT
import me.anon.grow3.util.DateUtils.TIME_FORMAT
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder

/**
 * Date util class for formatting/converting strings into date objects
 */
object DateUtils
{
	const val TIME_FORMAT = "HH:mm"
	const val CLOCK_FORMAT = "HH:mm"
	const val DATE_FORMAT = "dd/MM/yyyy"

	const val API_FORMAT = "yyyy-MM-dd'T'HH:mm"
	const val MID_DISPLAY_FORMAT = "dd MMM yyyy HH:mm"
	const val SHORT_DISPLAY_FORMAT_UK = "dd/MM/yy HH:mm"
	const val SHORT_DISPLAY_FORMAT_INT = "MM-dd-yy HH:mm"
	const val SHORT_DISPLAY_FORMAT_ISO = "yy-MM-dd HH:mm"
}

/**
 * Parses the given string as ISO-8601
 */
public fun String.asDateTime(): ZonedDateTime
	= ZonedDateTime.parse(this, DateTimeFormatterBuilder()
		.appendPattern(API_FORMAT)
		.appendOffset("+HH:mm", "+00:00")
		.toFormatter())

/**
 * Formats a zoned date time into API format for storage
 */
public fun ZonedDateTime.asApiString(): String
	= DateTimeFormatterBuilder()
		.appendPattern(API_FORMAT)
		.appendOffset("+HH:mm", "+00:00")
		.toFormatter()
		.format(this)

/**
 * Formats a zoned date time into a mid string
 * TODO: Add user display format setting
 */
public fun ZonedDateTime.asDisplayString(): String = format(DateTimeFormatter.ofPattern(MID_DISPLAY_FORMAT))

/**
 * Parses a display string into zoned date time. This will override the timezone to use current system default.
 * TODO: Allow for passing of stored zone to retain
 */
public fun String.fromDisplayString(): ZonedDateTime
	= ZonedDateTime.from(DateTimeFormatter.ofPattern(MID_DISPLAY_FORMAT).withZone(ZoneId.systemDefault()).parse(this))

public fun ZonedDateTime.formatDate(): String = format(DateTimeFormatter.ofPattern(DATE_FORMAT))
public fun ZonedDateTime.formatTime(): String = format(DateTimeFormatter.ofPattern(TIME_FORMAT))

//public fun LocalDate.asFormattedString(): String = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

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
