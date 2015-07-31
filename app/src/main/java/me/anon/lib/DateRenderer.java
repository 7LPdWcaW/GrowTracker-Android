package me.anon.lib;

public class DateRenderer
{
	public static final String SECONDS = "seconds";
	public static final String MINUTES = "minutes";
	public static final String HOURS = "hours";
	public static final String DAYS = "days";
	public static final String WEEKS = "weeks";
	public static final String MONTHS = "months";
	public static final String YEARS = "years";

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
			new Unit(SECONDS, sec, 60, 1),
			new Unit(MINUTES, min, 3600, 60),
			new Unit(HOURS, hour, 86400, 3600),
			new Unit(DAYS, day, 604800, 86400),
			new Unit(WEEKS, wk, 2629743, 604800),
			new Unit(MONTHS, mon, 31556926, 2629743),
			new Unit(YEARS, yr, 2629743, 31556926)
		};

		long currentTime = System.currentTimeMillis();
		int difference = (int)((currentTime - time) / 1000);

		if (difference < 5)
		{
			return new TimeAgo(units[0], now);
		}

		String formattedDate = null;
		Unit lastUnit = null;

		if (unitindex < 0)
		{
			for (Unit unit : units)
			{
				if (difference < unit.limit)
				{
					formattedDate = getFormattedDate(unit, difference);
					lastUnit = unit;
					break;
				}
			}
		}
		else
		{
			formattedDate = getFormattedDate(units[unitindex], difference);
			lastUnit = units[unitindex];
		}

		if (formattedDate == null)
		{
			lastUnit = units[units.length - 1];
			formattedDate = getFormattedDate(lastUnit, difference);
		}

		return new TimeAgo(lastUnit, formattedDate);
	}

	private String getFormattedDate(Unit unit, int difference)
	{
		int newDiff = (int)Math.floor(difference / unit.inSeconds);
		return String.format("%s%s", newDiff, unit.name);
	}

	public class TimeAgo
	{
		public Unit unit;
		public String formattedDate;

		public TimeAgo(Unit unit, String formattedDate)
		{
			this.unit = unit;
			this.formattedDate = formattedDate;
		}
	}

	public static class Unit
	{
		public String type;
		public String name;
		public int limit;
		public int inSeconds;

		public Unit(String type, String name, int limit, int inSeconds)
		{
			this.type = type;
			this.name = name;
			this.limit = limit;
			this.inSeconds = inSeconds;
		}
	}
}
