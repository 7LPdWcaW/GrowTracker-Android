package me.anon.grow3.util

import me.anon.grow3.util.DateUtils.API_FORMAT
import me.anon.grow3.util.DateUtils.DATETIME_MID_DISPLAY_FORMAT
import me.anon.grow3.util.DateUtils.DATE_FORMAT
import me.anon.grow3.util.DateUtils.DATE_MID_DISPLAY_FORMAT
import me.anon.grow3.util.DateUtils.TIME_FORMAT
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Date util class for formatting/converting strings into date objects
 */
object DateUtils
{
	const val TIME_FORMAT = "HH:mm"
	const val CLOCK_FORMAT = "HH:mm"
	const val DATE_FORMAT = "dd/MM/yyyy"

	const val API_FORMAT = "yyyy-MM-dd'T'HH:mm"

	const val DATETIME_MID_DISPLAY_FORMAT = "dd MMM yyyy HH:mm"
	const val DATETIME_SHORT_DISPLAY_FORMAT_UK = "dd/MM/yy HH:mm"
	const val DATETIME_SHORT_DISPLAY_FORMAT_INT = "MM-dd-yy HH:mm"
	const val DATETIME_SHORT_DISPLAY_FORMAT_ISO = "yy-MM-dd HH:mm"

	const val DATE_MID_DISPLAY_FORMAT = "EEE dd MMM yyyy"
	const val DATE_SHORT_DISPLAY_FORMAT_UK = "dd/MM/yy"
	const val DATE_SHORT_DISPLAY_FORMAT_INT = "MM-dd-yy"
	const val DATE_SHORT_DISPLAY_FORMAT_ISO = "yy-MM-dd"
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
 * Parses the given string as ISO-8601
 */
public fun String.asDate(): LocalDate
	= LocalDate.parse(this, DateTimeFormatterBuilder()
		.appendPattern(API_FORMAT)
		.appendOffset("+HH:mm", "+00:00")
		.toFormatter())

/**
 * Ago
 */
public fun String.ago(short: Boolean = true): String
{
	val now = ZonedDateTime.now()
	val then = asDateTime()
	val seconds = ChronoUnit.SECONDS.between(then, now)
	return when
	{
		seconds > 2629746L -> {
			val duration = ChronoUnit.MONTHS.between(then, now)
			duration.toString() + (if (short) "mo" else " month${if (duration > 1) "s" else ""} ago")
		}
		seconds > TimeUnit.DAYS.toSeconds(1) -> {
			val duration = ChronoUnit.DAYS.between(then, now)
			duration.toString() + (if (short) "d" else " day${if (duration > 1) "s" else ""} ago")
		}
		seconds > TimeUnit.HOURS.toSeconds(1) -> {
			val duration = ChronoUnit.HOURS.between(then, now)
			duration.toString() + (if (short) "h" else " hour${if (duration > 1) "s" else ""} ago")
		}
		seconds > TimeUnit.MINUTES.toSeconds(1) -> {
			val duration = ChronoUnit.MINUTES.between(then, now)
			duration.toString() + (if (short) "m" else " minute${if (duration > 1) "s" else ""} ago")
		}
		else -> {
			val duration = ChronoUnit.SECONDS.between(then, now)
			duration.toString() + (if (short) "s" else " second${if (duration > 1) "s" else ""} ago")
		}
	}
}

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
public fun ZonedDateTime.asDisplayString(): String = format(DateTimeFormatter.ofPattern(DATETIME_MID_DISPLAY_FORMAT))

/**
 * Formats a zoned date time into a mid string
 * TODO: Add user display format setting
 */
public fun LocalDate.asDisplayString(): String = format(DateTimeFormatter.ofPattern(DATE_MID_DISPLAY_FORMAT))

/**
 * Parses a display string into zoned date time. This will override the timezone to use current system default.
 * TODO: Allow for passing of stored zone to retain
 */
public fun String.fromDisplayString(): ZonedDateTime
	= ZonedDateTime.from(DateTimeFormatter.ofPattern(DATETIME_MID_DISPLAY_FORMAT).withZone(ZoneId.systemDefault()).parse(this))

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
