package me.anon.lib;

import android.content.Context;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Unit class used for measurement input
 */
@AllArgsConstructor
public enum Unit
{
	ML("ml")
	{
		public double from(Unit from, double fromValue)
		{
			return from.to(this, fromValue);
		}

		public double to(Unit to, double fromValue)
		{
			return fromValue;
		}
	};

	@Getter private String label;

	/**
	 * x to given unit
	 * @param to Unit to convert to
	 * @param fromValue ml value
	 * @return converted value
	 */
	public abstract double to(Unit to, double fromValue);

	/**
	 * Unit to x
	 * @param from Unit to convert to
	 * @param fromValue Unit value
	 * @return converted value
	 */
	public abstract double from(Unit from, double fromValue);

	public static Unit getSelectedDeliveryUnit(Context context)
	{
		return ML;
	}

	public static Unit getSelectedMeasurementUnit(Context context)
	{
		return ML;
	}
}
