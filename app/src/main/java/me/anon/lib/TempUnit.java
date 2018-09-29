package me.anon.lib;

import android.content.Context;
import android.preference.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Unit class used for measurement input
 */
public enum TempUnit
{
	KELVIN("K")
	{
		@Override public double to(TempUnit to, double fromValue)
		{
			switch (to)
			{
				case CELCIUS: return toTwoDecimalPlaces(fromValue + 273.15d);
				case FARENHEIT: return toTwoDecimalPlaces((fromValue * (9d / 5d)) - 459.67);
			}

			return fromValue;
		}
	},
	CELCIUS("C")
	{
		@Override public double to(TempUnit to, double fromValue)
		{
			switch (to)
			{
				case KELVIN: return toTwoDecimalPlaces(fromValue - 273.15d);
				case FARENHEIT: return toTwoDecimalPlaces((fromValue * 1.8) + 32);
			}

			return fromValue;
		}
	},
	FARENHEIT("F")
	{
		@Override public double to(TempUnit to, double fromValue)
		{
			switch (to)
			{
				case KELVIN: return toTwoDecimalPlaces((fromValue + 459.67) * (5d / 9d));
				case CELCIUS: return toTwoDecimalPlaces((fromValue - 32) / 1.8d);
			}

			return fromValue;
		}
	};

	private String label;

	private TempUnit(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	private static Double toTwoDecimalPlaces(double input)
	{
		return new BigDecimal(input).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
	}

	/**
	 * x to given unit
	 * @param to Unit to convert to
	 * @param fromValue ml value
	 * @return converted value
	 */
	public abstract double to(TempUnit to, double fromValue);

	public static TempUnit getSelectedTemperatureUnit(Context context)
	{
		int index;
		return values()[(index = PreferenceManager.getDefaultSharedPreferences(context).getInt("temperature_unit", -1)) == -1 ? CELCIUS.ordinal() : index];
	}
}
