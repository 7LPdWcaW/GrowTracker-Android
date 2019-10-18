package me.anon.lib;

import android.content.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;

import me.anon.grow.R;

public class DateRenderer
{
	private Context context;
	private final String now, sec, min, hour, day, wk, mon, yr;

	public DateRenderer()
	{
		now = "1s";
		sec = "s";
		min = "m";
		hour = "h";
		day = "d";
		wk = "w";
		mon = "mo";
		yr = "y";
	}

	public DateRenderer(Context context)
	{
		this.context = context.getApplicationContext();
		now = context.getString(R.string.now_abbr);
		sec = context.getString(R.string.second_abbr);
		min = context.getString(R.string.minute_abbr);
		hour = context.getString(R.string.hour_abbr);
		day = context.getString(R.string.day_abbr);
		wk = context.getString(R.string.week_abbr);
		mon = context.getString(R.string.month_abbr);
		yr = context.getString(R.string.year_abbr);
	}

	/**
	 * Converts a timestamp to how long ago syntax
	 *
	 * @param time The time in milliseconds
	 * @return The formatted time
	 */
	public TimeAgo timeAgo(double time)
	{
		return timeAgo(time, -1);
	}

	public TimeAgo timeAgo(double time, int unitindex)
	{
		TimeAgo result = null;

		Unit[] units = new Unit[]
		{
			new Unit(R.plurals.time_second, "seconds", sec, 60, 1),
			new Unit(R.plurals.time_minute, "minutes", min, 3600, 60),
			new Unit(R.plurals.time_hour, "hours", hour, 86400, 3600),
			new Unit(R.plurals.time_day, "days", day, 604800, 86400),
			new Unit(R.plurals.time_week, "weeks", wk, 2629743, 604800),
			new Unit(R.plurals.time_month, "months", mon, 31556926, 2629743),
			new Unit(R.plurals.time_year, "years", yr, 2629743, 31556926)
		};

		long currentTime = System.currentTimeMillis();
		double difference = (double)((double)(currentTime - time) / 1000d);

		String formattedDate = null;
		String longFormattedDate = null;
		Unit lastUnit = null;

		if (unitindex < 0)
		{
			if (difference < 5)
			{
				return new TimeAgo(units[0], round(difference / (double)units[0].inSeconds, 2), now, now);
			}

			for (Unit unit : units)
			{
				if (difference < unit.limit)
				{
					formattedDate = getFormattedDate(unit, difference);
					longFormattedDate = getLongFormattedDate(unit, difference);
					lastUnit = unit;
					break;
				}
			}
		}
		else
		{
			formattedDate = getFormattedDate(units[unitindex], difference);
			longFormattedDate = getLongFormattedDate(units[unitindex], difference);
			lastUnit = units[unitindex];
		}

		if (formattedDate == null)
		{
			lastUnit = units[units.length - 1];
			formattedDate = getFormattedDate(lastUnit, difference);
			longFormattedDate = getLongFormattedDate(lastUnit, difference);
		}

		return new TimeAgo(lastUnit, round(difference / (double)lastUnit.inSeconds, 2), formattedDate, longFormattedDate);
	}

	private static double round(double value, int places)
	{
		if (places < 0)
		{
			throw new IllegalArgumentException();
		}

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private String getFormattedDate(Unit unit, double difference)
	{
		int newDiff = (int)Math.floor(difference / unit.inSeconds);
		return String.format("%s%s", newDiff, unit.name);
	}

	private String getLongFormattedDate(Unit unit, double difference)
	{
		int newDiff = (int)Math.floor(difference / unit.inSeconds);

		if (context != null)
		{
			return String.format("%s %s", newDiff, context.getResources().getQuantityString(unit.pluralRes, newDiff));
		}

		return newDiff + " " + unit.enStr;
	}

	public class TimeAgo
	{
		public Unit unit;
		public double time;
		public String formattedDate;
		public String longFormattedDate;

		public TimeAgo(Unit unit, double time, String formattedDate, String longFormattedDate)
		{
			this.unit = unit;
			this.time = time;
			this.formattedDate = formattedDate;
			this.longFormattedDate = longFormattedDate;
		}
	}

	public static class Unit
	{
		public int pluralRes;
		public String enStr;
		public String name;
		public int limit;
		public int inSeconds;

		public Unit(int pluralRes, String enStr, String name, int limit, int inSeconds)
		{
			this.pluralRes = pluralRes;
			this.enStr = enStr;
			this.name = name;
			this.limit = limit;
			this.inSeconds = inSeconds;
		}
	}
}
